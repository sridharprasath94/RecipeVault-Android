package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.data.IngredientEntity
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.data.RecipeWithDetails
import com.flash.recipeVault.data.StepEntity
import com.flash.recipeVault.ui.theme.RecipeVaultTheme


private fun previewRecipeWithDetails(): RecipeWithDetails {
    val recipe = RecipeEntity(
        id = 1L,
        title = "Pasta Arrabbiata",
        description = "Spicy tomato pasta with garlic and chili flakes.",
        imageUri = null,
        imageUrl = "sample",
        isDeleted = false,
        deletedAt = null,
        createdAt = 0L,
        updatedAt = 0L,
    )

    val ingredients = listOf(
        IngredientEntity(
            id = 1L,
            recipeId = 1L,
            name = "Pasta",
            quantity = "200",
            unit = "g",
            sortOrder = 0
        ),
        IngredientEntity(
            id = 2L,
            recipeId = 1L,
            name = "Tomato",
            quantity = "2",
            unit = "pcs",
            sortOrder = 1
        ),
        IngredientEntity(
            id = 3L,
            recipeId = 1L,
            name = "Garlic",
            quantity = "2",
            unit = "cloves",
            sortOrder = 2
        ),
    )

    val steps = listOf(
        StepEntity(
            id = 1L,
            recipeId = 1L,
            instruction = "Boil pasta until al dente.",
            sortOrder = 0
        ),
        StepEntity(
            id = 2L,
            recipeId = 1L,
            instruction = "Cook tomato + garlic + chili.",
            sortOrder = 1
        ),
        StepEntity(id = 3L, recipeId = 1L, instruction = "Mix and serve.", sortOrder = 2),
    )

    return RecipeWithDetails(recipe = recipe, ingredients = ingredients, steps = steps)
}

@Preview(name = "TopBar", showBackground = true, widthDp = 360)
@Composable
private fun RecipeDetailTopBarPreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                RecipeDetailTopBar(
                    recipeId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDelete = {},
                )
            }
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            )
        }
    }
}

@Preview(name = "Content - Loading", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeDetailContentPreview_Loading() {
    RecipeVaultTheme {
        RecipeDetailContent(
            padding = PaddingValues(0.dp),
            data = null,
        )
    }
}

@Preview(name = "Content - Loaded", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeDetailContentPreview_Loaded() {
    RecipeVaultTheme {
        RecipeDetailContent(
            padding = PaddingValues(0.dp),
            data = previewRecipeWithDetails(),
        )
    }
}