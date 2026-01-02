package com.flash.recipeVault.ui.screens.editRecipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.tooling.preview.Preview
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

private fun fakeEditRecipeUiState(
    ingredients: List<IngredientFormRow> = previewIngredients(),
    steps: List<String> = previewSteps(),
    pickedImageUri: String? = null,
    existingImageUrl: String? = null,
    isLoadingData: Boolean = false,
    isSaving: Boolean = false,
): EditRecipeUiState {
    return EditRecipeUiState(
        title = "Paneer Butter Masala",
        description = "Rich and creamy curry",
        ingredients = ingredients,
        suggestions = previewSuggestions(),
        pickedImageUri = pickedImageUri,
        existingImageUrl = existingImageUrl,
        isLoadingData = isLoadingData,
        steps = steps,
        isSaving = isSaving,
    )
}

@Preview(
    name = "Edit Recipe Content Loaded With Picked Image",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Edit Recipe Content Loaded With Picked Image - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditRecipeContentLoadedPreview() {
    RecipeVaultTheme {
        EditRecipeContent(
            ui = fakeEditRecipeUiState(pickedImageUri = "content://sample/picked/image.jpg"),
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
}

@Preview(
    name = "Edit Recipe Content Loading Data",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Edit Recipe Content Loading Data",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditRecipeContentLoadingDataPreview() {
    RecipeVaultTheme {
        EditRecipeContent(
            ui = fakeEditRecipeUiState(isLoadingData = true),
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
}

@Preview(
    name = "Edit Recipe Content Saving",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Edit Recipe Content Saving - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditRecipeContentSavingPreview() {
    RecipeVaultTheme {
        EditRecipeContent(
            ui = fakeEditRecipeUiState(isSaving = true),
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
}

@Preview(
    name = "Edit Recipe Content Loaded With Existing Image",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Edit Recipe Content Loaded With Existing Image - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditRecipeContentLoadedExistingImagePreview() {
    RecipeVaultTheme {
        EditRecipeContent(
            ui = fakeEditRecipeUiState(existingImageUrl = "https://example.com/existing/image.jpg"),
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
}


