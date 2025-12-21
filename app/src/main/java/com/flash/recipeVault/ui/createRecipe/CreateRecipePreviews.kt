package com.flash.recipeVault.ui.createRecipe

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

@Preview(name = "TopBar", showBackground = true, widthDp = 360)
@Composable
private fun CreateRecipeTopBarPreview() {
    RecipeVaultTheme {
        Scaffold(
            topBar = {
                CreateRecipeTopBar(
                    title = "New Recipe",
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
            imageUri = "sample_image",
            onPickImage = {},
            onRemoveImage = {},
            ingredients = remember { mutableStateListOf(IngredientFormRow()) },
            onIngredientChange = { _, _ -> },
            onIngredientRemove = { _ -> },
            onAddIngredient = {},
            steps = remember { mutableStateListOf("") },
            onStepChange = { _, _ -> },
            onAddStep = {},
            error = null,
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
            imageUri = "sample_image",
            onPickImage = {},
            onRemoveImage = {},
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepChange = { idx, v -> steps[idx] = v },
            onAddStep = { steps.add("") },
            error = null,
        )
    }
}

@Preview(name = "Basic Fields", showBackground = true, widthDp = 360)
@Composable
private fun RecipeBasicFieldsPreview() {
    RecipeVaultTheme {
        RecipeBasicFields(
            title = "Masala Omelette",
            onTitleChange = {},
            desc = "Eggs with onion, chili, coriander.",
            onDescChange = {},
        )
    }
}

@Preview(name = "Image Picker - No Image", showBackground = true, widthDp = 360)
@Composable
private fun RecipeImagePickerSectionPreview_NoImage() {
    RecipeVaultTheme {
        RecipeImagePickerSection(
            imageUri = "sample_image",
            onPickImage = {},
            onRemoveImage = {},
        )
    }
}

@Preview(name = "Image Picker - With Image", showBackground = true, widthDp = 360)
@Composable
private fun RecipeImagePickerSectionPreview_WithImage() {
    RecipeVaultTheme {
        RecipeImagePickerSection(
            imageUri = "content://com.example.fake/image/1",
            onPickImage = {},
            onRemoveImage = {},
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
            imageUri = "sample_image",
            onPickImage = {},
            onRemoveImage = {},
            ingredients = remember { mutableStateListOf(IngredientFormRow()) },
            onIngredientChange = { _, _ -> },
            onIngredientRemove = { _ -> },
            onAddIngredient = {},
            steps = remember { mutableStateListOf("") },
            onStepChange = { _, _ -> },
            onAddStep = {},
            error = "Title is required",
        )
    }
}

