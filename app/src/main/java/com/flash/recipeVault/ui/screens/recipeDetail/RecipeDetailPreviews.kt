package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.theme.RecipeVaultTheme


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
    name = "Recipe Detail – Loaded",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Preview(
    name = "Recipe Detail – Loaded - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun RecipeDetailContentLoadedPreview() {
    RecipeVaultTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi(),
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
@Preview(
    name = "Recipe Detail – Loading - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun RecipeDetailContentLoadingPreview() {
    RecipeVaultTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi().copy(isLoadingData = true),
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
@Preview(
    name = "Recipe Detail – Finishing - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun RecipeDetailContentFinishingPreview() {
    RecipeVaultTheme {
        RecipeDetailContent(
            ui = previewRecipeDetailUi().copy(isNavigating = true),
            onBack = {},
            onEdit = {},
            onDelete = {}
        )
    }
}