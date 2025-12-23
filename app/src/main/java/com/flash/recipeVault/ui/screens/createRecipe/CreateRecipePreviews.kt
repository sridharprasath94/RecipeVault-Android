package com.flash.recipeVault.ui.screens.createRecipe

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.components.IngredientFormRow
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

@Preview(name = "TopBar - isSavingFalse", showBackground = true, widthDp = 360)
@Composable
private fun CreateRecipeTopBarIsSavingFalsePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                CreateRecipeTopBar(
                    title = "New Recipe",
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

@Preview(name = "TopBar - isSavingTrue", showBackground = true, widthDp = 360)
@Composable
private fun CreateRecipeTopBarIsSavingTruePreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                CreateRecipeTopBar(
                    title = "New Recipe",
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


@Preview(name = "Form - Empty", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CreateRecipeFormPreview_Empty() {
    RecipeVaultTheme {
        CreateRecipeForm(
            padding = PaddingValues(0.dp),
            title = "",
            onTitleChange = {},
            desc = "",
            onDescChange = {},
            imageUri = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = remember { mutableStateListOf(IngredientFormRow()) },
            onIngredientChange = { _, _ -> },
            onIngredientRemove = { _ -> },
            onAddIngredient = {},
            steps = remember { mutableStateListOf("") },
            onStepChange = { _, _ -> },
            onStepsRemove = { _ -> },
            onAddStep = {},
        )
    }
}

@Preview(name = "Form - Filled", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CreateRecipeFormPreview_Filled() {
    val ingredients = previewIngredients()
    val steps = previewSteps()

    RecipeVaultTheme {
        CreateRecipeForm(
            padding = PaddingValues(0.dp),
            title = "Pasta Arrabbiata",
            onTitleChange = {},
            desc = "Spicy tomato pasta with garlic and chili flakes.",
            onDescChange = {},
            imageUri = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepsRemove = { _ -> },
            onStepChange = { idx, v -> steps[idx] = v },
            onAddStep = { steps.add("") },
        )
    }
}

@Preview(name = "Error Text", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun CreateRecipeFormPreview_Error() {
    RecipeVaultTheme {
        CreateRecipeForm(
            padding = PaddingValues(0.dp),
            title = "",
            onTitleChange = {},
            desc = "",
            onDescChange = {},
            imageUri = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = remember { mutableStateListOf(IngredientFormRow()) },
            onIngredientChange = { _, _ -> },
            onIngredientRemove = { _ -> },
            onAddIngredient = {},
            steps = remember { mutableStateListOf("") },
            onStepsRemove = { _ -> },
            onStepChange = { _, _ -> },
            onAddStep = {},
        )
    }
}

