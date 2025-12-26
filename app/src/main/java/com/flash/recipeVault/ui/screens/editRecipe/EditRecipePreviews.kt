package com.flash.recipeVault.ui.screens.editRecipe

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

private fun previewIngredientsState() = mutableStateListOf(
    IngredientFormRow(name = "Pasta", qty = "200", unit = "g"),
    IngredientFormRow(name = "Tomato", qty = "2", unit = "pcs"),
    IngredientFormRow(name = "Garlic", qty = "2", unit = "cloves"),
)

private fun previewStepsState() = mutableStateListOf(
    "Boil pasta until al dente.",
    "Cook tomato + garlic + chili.",
    "Mix and serve.",
)

@Preview(name = "Edit TopBar - isSavingFalse", showBackground = true, widthDp = 360)
@Composable
private fun EditRecipeTopBarIsSavingFalsePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                EditRecipeTopBar(
                    title = "Edit Recipe",
                    isSaving = false,
                    onBack = {},
                    onSave = {},
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

@Preview(name = "Edit TopBar - isSavingTrue", showBackground = true, widthDp = 360)
@Composable
private fun EditRecipeTopBarIsSavingTruePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                EditRecipeTopBar(
                    title = "Edit Recipe",
                    isSaving = true,
                    onBack = {},
                    onSave = {},
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

@Preview(name = "Edit Form - Loading", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EditRecipeFormPreview_Loading() {
    val ingredients = remember { mutableStateListOf(IngredientFormRow()) }
    val steps = remember { mutableStateListOf("") }

    RecipeVaultTheme {
        EditRecipeForm(
            padding = PaddingValues(0.dp),
            isLoading = true,
            title = "",
            suggestions =  listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
            onTitleChange = {},
            desc = "",
            onDescChange = {},
            pickedImageUri = null,
            alreadyAvailableImageUrl = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepChange = { idx, v -> steps[idx] = v },
            onStepsRemove = { idx -> if (steps.size > 1) steps.removeAt(idx) },
            onAddStep = { steps.add("") },
        )
    }
}

@Preview(name = "Edit Form - Loaded", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EditRecipeFormPreview_Loaded() {
    val ingredients = previewIngredientsState()
    val steps = previewStepsState()

    RecipeVaultTheme {
        EditRecipeForm(
            padding = PaddingValues(0.dp),
            isLoading = false,
            title = "Pasta Arrabbiata",
            suggestions =  listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
            onTitleChange = {},
            desc = "Spicy tomato pasta with garlic and chili flakes.",
            onDescChange = {},
            pickedImageUri = null,
            // Note: Preview may not load network images; this previews layout only.
            alreadyAvailableImageUrl = "https://example.com/sample.jpg",
            onPickImage = {},
            onRemoveImage = {},
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepChange = { idx, v -> steps[idx] = v },
            onStepsRemove = { idx -> if (steps.size > 1) steps.removeAt(idx) },
            onAddStep = { steps.add("") },
        )
    }
}

@Preview(name = "Edit Form - Error", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun EditRecipeFormPreview_Error() {
    val ingredients = previewIngredientsState()
    val steps = previewStepsState()

    RecipeVaultTheme {
        EditRecipeForm(
            padding = PaddingValues(0.dp),
            isLoading = false,
            title = "",
            suggestions =  listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
            onTitleChange = {},
            desc = "",
            onDescChange = {},
            pickedImageUri = null,
            alreadyAvailableImageUrl = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepChange = { idx, v -> steps[idx] = v },
            onStepsRemove = { idx -> if (steps.size > 1) steps.removeAt(idx) },
            onAddStep = { steps.add("") },
        )
    }
}
