package com.flash.recipeVault.ui.screens.editRecipe

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.components.FormTopBar
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

private fun previewIngredients() = mutableStateListOf(
    IngredientFormRow(name = "Pasta", qty = "200", unit = "g"),
    IngredientFormRow(name = "Tomato", qty = "2", unit = "pcs"),
    IngredientFormRow(name = "Garlic", qty = "2", unit = "cloves"),
)

private fun previewSteps() = mutableStateListOf(
    "Boil pasta until al dente.",
    "Cook tomato + garlic + chili.",
    "Mix and serve.",
)

private fun previewSuggestions() = SuggestionsUi(
    ingredients = listOf("Pasta", "Tomato", "Garlic", "Chili", "Basil"),
    units = listOf("g", "pcs", "cloves", "tbsp", "tsp"),
    steps = listOf("Boil", "Cook", "Mix", "Serve")
)

@Preview(
    name = "Edit TopBar - isSavingFalse",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Composable
private fun EditRecipeTopBarIsSavingFalsePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                FormTopBar(
                    title = "Edit Recipe",
                    actionLabel = "Save",
                    isInteractionEnabled = false,
                    onBack = {},
                    onPrimaryAction = {},
                )
            }
        ) { padding ->
            Spacer(
                Modifier
                    .padding(padding)
                    .height(1.dp)
            )
        }
    }
}

@Preview(name = "Edit TopBar - isSavingTrue", showBackground = true, widthDp = 550, heightDp = 1300)
@Composable
private fun EditRecipeTopBarIsSavingTruePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                FormTopBar(
                    title = "Edit Recipe",
                    actionLabel = "Save",
                    isInteractionEnabled = false,
                    onBack = {},
                    onPrimaryAction = {},
                )
            }
        ) { padding ->
            Spacer(
                Modifier
                    .padding(padding)
                    .height(1.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 550, heightDp = 1300)
@Composable
fun EditRecipeScreenPreview() {
    EditRecipeContent(
        ui = EditRecipeUiState(
            title = "Butter Chicken",
            description = "Classic creamy Indian curry",
            ingredients = previewIngredients(),
            steps = previewSteps(),
            pickedImageUri = null,
            existingImageUrl = "https://example.com/image.jpg",
            isSaving = false,
            isLoadingData = false
        ),
        suggestions = previewSuggestions(),
        isNavigating = false,
        onBack = {},
        onSave = {},
        onTitleChange = {},
        onDescChange = {},
        onPickImage = {},
        onRemoveImage = {},
        onIngredientChange = { _, _ -> },
        onIngredientRemove = {},
        onIngredientAdd = {},
        onStepChange = { _, _ -> },
        onStepRemove = {},
        onStepAdd = {}
    )
}
