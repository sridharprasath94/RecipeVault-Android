package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.ui.components.IngredientFormRow


private fun previewRecipeDetailUi(): RecipeDetailUiState {
    return RecipeDetailUiState(
        title = "Paneer Butter Masala",
        description = "A rich and creamy North Indian curry made with paneer.",
        existingImageUrl = null, // keep null for preview stability
        ingredients = listOf(
            IngredientFormRow("Paneer", "200", "g"),
            IngredientFormRow("Butter", "2", "tbsp"),
            IngredientFormRow("Tomato", "3", "pcs"),
        ),
        steps = listOf(
            "Heat butter in a pan",
            "Add tomatoes and cook until soft",
            "Blend and add paneer",
        ),
        isLoadingData = false,
        deleteRecipeId = null
    )
}

@Preview(
    name = "Recipe Detail – Normal",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
fun RecipeDetailContentPreview() {
    MaterialTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi(),
            isNavigating = false,
            onBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(
    name = "Recipe Detail – Loading",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
fun RecipeDetailContentLoadingPreview() {
    MaterialTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi().copy(isLoadingData = true),
            isNavigating = false,
            onBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}

@Preview(
    name = "Recipe Detail – Finishing",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
fun RecipeDetailContentFinishingPreview() {
    MaterialTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi(),
            isNavigating = true,
            onBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}