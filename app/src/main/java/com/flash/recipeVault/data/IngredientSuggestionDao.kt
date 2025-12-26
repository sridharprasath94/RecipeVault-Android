package com.flash.recipeVault.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientSuggestionDao {

    @Query("""
        SELECT * FROM ingredient_suggestions
        WHERE nameLower LIKE :prefix || '%'
        ORDER BY nameLower
        LIMIT :limit
    """)
    suspend fun searchPrefix(prefix: String, limit: Int = 20): List<IngredientSuggestionEntity>

    @Query("SELECT * FROM ingredient_suggestions ORDER BY nameLower")
    fun observeAll(): Flow<List<IngredientSuggestionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: IngredientSuggestionEntity)

    suspend fun insertIfValid(name: String) {
        val clean = name.trim()
        if (clean.length < 2) return
        insert(IngredientSuggestionEntity(nameLower = clean.lowercase(), name = clean))
    }
}