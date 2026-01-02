package com.flash.recipeVault.ui.screens.createRecipe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

private fun previewIngredients(): SnapshotStateList<IngredientFormRow> =
    mutableStateListOf(
        IngredientFormRow(name = "Pasta", qty = "200", unit = "g"),
        IngredientFormRow(name = "Tomato", qty = "2", unit = "pcs"),
        IngredientFormRow(name = "Garlic", qty = "2", unit = "cloves"),
    )

private fun previewSteps(): SnapshotStateList<String> =
    mutableStateListOf(
        "Boil pasta until al dente.",
        "Cook tomato + garlic + chili.",
        "Mix and serve.",
    )

private fun previewSuggestions() = SuggestionsUi(
    ingredients = listOf("Pasta", "Tomato", "Garlic", "Chili", "Basil"),
    units = listOf("g", "pcs", "cloves", "tbsp", "tsp"),
    steps = listOf("Boil", "Cook", "Mix", "Serve")
)

private fun fakeCreateRecipeUiState(
    ingredients: List<IngredientFormRow> = previewIngredients(),
    steps: List<String> = previewSteps(),
    pickedImageUrl: String? = null,
    isSaving: Boolean = false,
): CreateRecipeUiState {
    return CreateRecipeUiState(
        title = "Paneer Butter Masala",
        description = "Rich and creamy curry",
        ingredients = ingredients,
        suggestions = previewSuggestions(),
        pickedImageUri = pickedImageUrl,
        steps = steps,
        isSaving = isSaving,
    )
}

@Preview(
    name = "Create Recipe Content Loaded",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Create Recipe Content Loaded - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateRecipeContentLoadedPreview() {
    RecipeVaultTheme {
        CreateRecipeContent(
            ui = fakeCreateRecipeUiState(),
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
    name = "Create Recipe Content Saving",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Create Recipe Content Saving - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateRecipeContentSavingPreview() {
    RecipeVaultTheme {
        CreateRecipeContent(
            ui = fakeCreateRecipeUiState(isSaving = true),
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
    name = "Create Recipe Content Empty Ingredients and Steps",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Create Recipe Content Empty Ingredients and Steps- Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateRecipeContentEmptyIngredientsAndStepsPreview() {
    RecipeVaultTheme {
        CreateRecipeContent(
            ui = fakeCreateRecipeUiState(ingredients = emptyList(), steps = emptyList()),
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
    name = "Create Recipe Content With Picked Image",
    showBackground = true,
    widthDp = 550,
    heightDp = 1300
)
@Preview(
    name = "Create Recipe Content With Picked Image - Dark",
    showBackground = true,
    widthDp = 550, heightDp = 1300,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CreateRecipeContentWithPickedImagePreview() {
    RecipeVaultTheme {
        CreateRecipeContent(
            ui = fakeCreateRecipeUiState(pickedImageUrl = "content://picked-image-uri"),
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

