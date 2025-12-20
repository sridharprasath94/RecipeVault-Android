package com.flash.recipeVault.firebase

import com.flash.recipeVault.data.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseBackupService(
    private val repo: RecipeRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun backupNow() {
        val uid = auth.currentUser?.uid ?: return
        val json = repo.exportAllAsJson()
        val doc = mapOf(
            "updatedAt" to System.currentTimeMillis(),
            "data" to json
        )
        firestore.collection("users").document(uid)
            .collection("backups").document("latest")
            .set(doc).await()
    }

    suspend fun syncFromCloudIfAvailable() {
        val uid = auth.currentUser?.uid ?: return
        val snap = firestore.collection("users").document(uid)
            .collection("backups").document("latest")
            .get().await()

        val data = snap.getString("data") ?: return
        repo.importFromJson(data)
    }
}
