package com.flash.recipeVault.data

import androidx.room.*
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun observeRecipes(): Flow<List<RecipeEntity>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipeWithDetails(id: Long): Flow<RecipeWithDetails?>

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert
    suspend fun insertIngredients(items: List<IngredientEntity>)

    @Insert
    suspend fun insertSteps(items: List<StepEntity>)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Query("UPDATE recipes SET title = :title, description = :description, imageUri = :imageUri, imageUrl = :imageUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRecipe(id: Long, title: String, description: String?, imageUri: String?, imageUrl: String?, updatedAt: Long)

    @Query("UPDATE recipes SET imageUrl = :imageUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateImageUrl(id: Long, imageUrl: String?, updatedAt: Long)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredients(recipeId: Long)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteSteps(recipeId: Long)

    @Transaction
    @Query("SELECT * FROM recipes WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    suspend fun getAllWithDetails(): List<RecipeWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Query("DELETE FROM ingredients")
    suspend fun clearIngredients()

    @Query("DELETE FROM steps")
    suspend fun clearSteps()

    @Query("DELETE FROM recipes")
    suspend fun clearRecipes()

    @Transaction
    suspend fun clearAll() {
        clearIngredients()
        clearSteps()
        clearRecipes()
    }


    // ✅ SINGLE @Query, SINGLE method
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeOnce(id: Long): RecipeEntity?

    // ✅ Proper annotation added
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id LIMIT 1")
    suspend fun getRecipeWithDetailsOnce(id: Long): RecipeWithDetails?

    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    suspend fun getAllRecipesIncludingDeletedOnce(): List<RecipeEntity>

    @Query("UPDATE recipes SET isDeleted = 1, deletedAt = :deletedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markRecipeDeleted(id: Long, deletedAt: Long, updatedAt: Long)
}
