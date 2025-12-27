package com.flash.recipeVault.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suggestions")
data class SuggestionEntity(
    @PrimaryKey val key: String,
    val type: SuggestionType,
    val value: String,
    val valueLower: String,
    val createdAt: Long = System.currentTimeMillis(),
)

enum class SuggestionType { INGREDIENT, STEP, UNIT }