package com.flash.recipeVault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient_suggestions")
data class IngredientSuggestionEntity(
    @PrimaryKey val nameLower: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)