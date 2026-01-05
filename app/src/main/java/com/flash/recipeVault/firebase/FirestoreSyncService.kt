package com.flash.recipeVault.firebase

import com.flash.recipeVault.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Two-way sync: per-recipe documents under users/<uid>/recipes/<recipeId>.
 * Conflict handling: last-write-wins using updatedAt.
 */
class FirestoreSyncService(
    private val repo: RecipeRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun uid(): String = auth.currentUser?.uid ?: error("User not logged in")

    private var listener: ListenerRegistration? = null

    private fun userRecipesCol() =
        firestore.collection("users").document(uid()).collection("recipes")

    suspend fun syncNow() {

        // 1) Pull remote -> local (merge by updatedAt)
        val remoteSnap = userRecipesCol().get().await()
        for (doc in remoteSnap.documents) {
            val id = doc.id.toLongOrNull() ?: continue
            val updatedAt = doc.getLong("updatedAt") ?: 0L
            val title = doc.getString("title") ?: ""
            val description = doc.getString("description")
            val imageUri = doc.getString("imageUri")
            val imageUrl = doc.getString("imageUrl")
            val createdAt = doc.getLong("createdAt") ?: updatedAt
            val isDeleted = doc.getBoolean("isDeleted") ?: false
            val deletedAt = doc.getLong("deletedAt")

            val recipe = RecipeEntity(
                id = id,
                title = title,
                description = description,
                imageUri = imageUri,
                imageUrl = imageUrl,
                isDeleted = isDeleted,
                deletedAt = deletedAt,
                createdAt = createdAt,
                updatedAt = updatedAt
            )

            val ingredients = (doc.get("ingredients") as? List<*>)?.mapIndexedNotNull { idx, v ->
                val m = v as? Map<*, *> ?: return@mapIndexedNotNull null
                IngredientEntity(
                    recipeId = id,
                    name = (m["name"] as? String) ?: return@mapIndexedNotNull null,
                    quantity = m["quantity"] as? String,
                    unit = m["unit"] as? String,
                    sortOrder = (m["sortOrder"] as? Number)?.toInt() ?: idx
                )
            } ?: emptyList()

            val steps = (doc.get("steps") as? List<*>)?.mapIndexedNotNull { idx, v ->
                val m = v as? Map<*, *> ?: return@mapIndexedNotNull null
                StepEntity(
                    recipeId = id,
                    instruction = (m["instruction"] as? String) ?: return@mapIndexedNotNull null,
                    sortOrder = (m["sortOrder"] as? Number)?.toInt() ?: idx
                )
            } ?: emptyList()

            repo.applyRemoteRecipe(recipe, ingredients, steps)
        }

        // 2) Push local -> remote (merge by updatedAt)
        val locals = repo.getAllLocalRecipesIncludingDeleted()
        for (local in locals) {
            val docRef = userRecipesCol().document(local.id.toString())
            val remote = docRef.get().await()
            val remoteUpdated = remote.getLong("updatedAt") ?: -1L
            if (!remote.exists() || local.updatedAt > remoteUpdated) {
                val details = repo.getRecipeWithDetailsOnce(local.id)
                val ing = details?.ingredients?.sortedBy { it.sortOrder } ?: emptyList()
                val st = details?.steps?.sortedBy { it.sortOrder } ?: emptyList()

                val payload = hashMapOf(
                    "id" to local.id,
                    "title" to local.title,
                    "description" to local.description,
                    "imageUri" to local.imageUri,
                    "imageUrl" to local.imageUrl,
                    "createdAt" to local.createdAt,
                    "updatedAt" to local.updatedAt,
                    "isDeleted" to local.isDeleted,
                    "deletedAt" to local.deletedAt,
                    "ingredients" to ing.map {
                        mapOf(
                            "name" to it.name,
                            "quantity" to it.quantity,
                            "unit" to it.unit,
                            "sortOrder" to it.sortOrder
                        )
                    },
                    "steps" to st.map {
                        mapOf(
                            "instruction" to it.instruction,
                            "sortOrder" to it.sortOrder
                        )
                    }
                ).filterValues { it != null } as HashMap<String, Any?>

                docRef.set(payload).await()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startRealtime() {
        if (listener != null) return
        listener = userRecipesCol().addSnapshotListener { snapshots, err ->
            if (err != null || snapshots == null) return@addSnapshotListener

            // Apply remote changes incrementally
            for (change in snapshots.documentChanges) {
                val doc = change.document
                val id = doc.id.toLongOrNull() ?: continue

                val updatedAt = doc.getLong("updatedAt") ?: 0L
                val title = doc.getString("title") ?: ""
                val description = doc.getString("description")
                val imageUri = doc.getString("imageUri")
                val imageUrl = doc.getString("imageUrl")
                val createdAt = doc.getLong("createdAt") ?: updatedAt
                val isDeleted = doc.getBoolean("isDeleted") ?: false
                val deletedAt = doc.getLong("deletedAt")

                val recipe = RecipeEntity(
                    id = id,
                    title = title,
                    description = description,
                    imageUri = imageUri,
                    imageUrl = imageUrl,
                    isDeleted = isDeleted,
                    deletedAt = deletedAt,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )

                val ingredients =
                    (doc.get("ingredients") as? List<*>)?.mapIndexedNotNull { idx, v ->
                        val m = v as? Map<*, *> ?: return@mapIndexedNotNull null
                        IngredientEntity(
                            recipeId = id,
                            name = (m["name"] as? String) ?: return@mapIndexedNotNull null,
                            quantity = m["quantity"] as? String,
                            unit = m["unit"] as? String,
                            sortOrder = (m["sortOrder"] as? Number)?.toInt() ?: idx
                        )
                    } ?: emptyList()

                val steps = (doc.get("steps") as? List<*>)?.mapIndexedNotNull { idx, v ->
                    val m = v as? Map<*, *> ?: return@mapIndexedNotNull null
                    StepEntity(
                        recipeId = id,
                        instruction = (m["instruction"] as? String)
                            ?: return@mapIndexedNotNull null,
                        sortOrder = (m["sortOrder"] as? Number)?.toInt() ?: idx
                    )
                } ?: emptyList()

                // Fire-and-forget; Room writes are suspend so we launch a coroutine via a lightweight thread.
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching { repo.applyRemoteRecipe(recipe, ingredients, steps) }
                }
            }
        }
    }

    fun stopRealTime() {
        listener?.remove()
        listener = null
    }
}
