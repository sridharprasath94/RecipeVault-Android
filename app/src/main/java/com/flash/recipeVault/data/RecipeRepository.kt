package com.flash.recipeVault.data

class RecipeRepository(private val dao: RecipeDao) {

    fun observeRecipes() = dao.observeRecipes()
    fun observeRecipe(id: Long) = dao.observeRecipeWithDetails(id)

    suspend fun createRecipe(
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl: String?,
        ingredients: List<Triple<String, String?, String?>>,
        steps: List<String>
    ): Long {
        val now = System.currentTimeMillis()
        val recipeId = dao.insertRecipe(
            RecipeEntity(
                title = title,
                description = description,
                imageUri = imageUri,
                imageUrl = imageUrl,
                createdAt = now,
                updatedAt = now
            )
        )

        dao.insertIngredients(
            ingredients.mapIndexed { idx, (name, qty, unit) ->
                IngredientEntity(
                    recipeId = recipeId,
                    name = name,
                    quantity = qty,
                    unit = unit,
                    sortOrder = idx
                )
            }
        )

        dao.insertSteps(
            steps.mapIndexed { idx, text ->
                StepEntity(recipeId = recipeId, instruction = text, sortOrder = idx)
            }
        )

        return recipeId
    }

    suspend fun deleteRecipe(id: Long) {
        val now = System.currentTimeMillis()

        // Soft-delete (tombstone) so two-way Firestore sync will not resurrect deleted recipes.
        dao.markRecipeDeleted(id = id, deletedAt = now, updatedAt = now)

        // Optional: remove children locally to save space (the recipe row remains as a tombstone).
        dao.deleteIngredients(id)
        dao.deleteSteps(id)
    }

    suspend fun updateLocalImageUrl(id: Long, imageUrl: String) {
        dao.updateImageUrl(
            id = id,
            imageUrl = imageUrl,
            updatedAt = System.currentTimeMillis()
        )
    }

    suspend fun updateRecipe(
        id: Long,
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl: String?,
        ingredients: List<Triple<String, String?, String?>>,
        steps: List<String>
    ) {
        val now = System.currentTimeMillis()
        dao.updateRecipe(
            id = id,
            title = title,
            description = description,
            imageUri = imageUri,
            imageUrl = imageUrl,
            updatedAt = now
        )

        dao.deleteIngredients(id)
        dao.deleteSteps(id)

        dao.insertIngredients(
            ingredients.mapIndexed { idx, (name, qty, unit) ->
                IngredientEntity(
                    recipeId = id,
                    name = name,
                    quantity = qty,
                    unit = unit,
                    sortOrder = idx
                )
            }
        )

        dao.insertSteps(
            steps.mapIndexed { idx, text ->
                StepEntity(recipeId = id, instruction = text, sortOrder = idx)
            }
        )
    }

    suspend fun exportAllAsJson(): String {
        val list = dao.getAllWithDetails()
        val export = list.map { r ->
            mapOf(
                "recipe" to mapOf(
                    "id" to r.recipe.id,
                    "title" to r.recipe.title,
                    "description" to r.recipe.description,
                    "imageUri" to r.recipe.imageUri,
                    "imageUrl" to r.recipe.imageUrl,
                    "createdAt" to r.recipe.createdAt,
                    "updatedAt" to r.recipe.updatedAt,
                    "isDeleted" to r.recipe.isDeleted,
                    "deletedAt" to r.recipe.deletedAt
                ),
                "ingredients" to r.ingredients.sortedBy { it.sortOrder }.map {
                    mapOf(
                        "name" to it.name,
                        "quantity" to it.quantity,
                        "unit" to it.unit,
                        "sortOrder" to it.sortOrder
                    )
                },
                "steps" to r.steps.sortedBy { it.sortOrder }.map {
                    mapOf("instruction" to it.instruction, "sortOrder" to it.sortOrder)
                }
            )
        }
        return com.flash.recipeVault.util.SimpleJson.encode(export)
    }


    suspend fun importFromJson(json: String) {
        val parsed = com.flash.recipeVault.util.JsonImportParser.parse(json)
        dao.clearAll()
        dao.insertRecipes(parsed.recipes)
        dao.insertIngredients(parsed.ingredients)
        dao.insertSteps(parsed.steps)
    }

    suspend fun getAllLocalRecipesIncludingDeleted(): List<RecipeEntity> {
        return dao.getAllRecipesIncludingDeletedOnce()
    }

    suspend fun getRecipeWithDetailsOnce(id: Long) = dao.getRecipeWithDetailsOnce(id)

    /**
     * Apply remote state with last-write-wins using updatedAt.
     */
    suspend fun applyRemoteRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>
    ) {
        val local = dao.getRecipeOnce(recipe.id)
        if (local == null || recipe.updatedAt >= local.updatedAt) {
            // If remote says deleted, apply tombstone locally and don't re-insert children.
            if (recipe.isDeleted) {
                dao.markRecipeDeleted(
                    id = recipe.id,
                    deletedAt = recipe.deletedAt ?: recipe.updatedAt,
                    updatedAt = recipe.updatedAt
                )
                dao.deleteIngredients(recipe.id)
                dao.deleteSteps(recipe.id)
                return
            }

            // Upsert recipe
            dao.insertRecipes(listOf(recipe))

            // Replace children
            dao.deleteIngredients(recipe.id)
            dao.deleteSteps(recipe.id)
            dao.insertIngredients(ingredients)
            dao.insertSteps(steps)
        }
    }
}
