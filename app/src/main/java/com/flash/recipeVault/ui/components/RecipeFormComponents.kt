package com.flash.recipeVault.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.model.SuggestionsUi
import com.flash.recipeVault.ui.screens.recipeDetail.SectionCard
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

data class IngredientFormRow(
    val name: String = "",
    val qty: String = "",
    val unit: String = ""
)

@Composable
fun IngredientFormField(
    index: Int,
    suggestions: SuggestionsUi,
    row: IngredientFormRow,
    onChange: (IngredientFormRow) -> Unit,
    onRemove: (() -> Unit)? = null,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionAutoCompleteField(
                value = row.name,
                onValueChange = {
                    onChange(row.copy(name = it))
                },
                suggestions = suggestions.ingredients,
                label = "Ingredient",
                showDropdownIcon = true,
                matchMode = MatchMode.Contains
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = row.qty,
                    onValueChange = { newValue ->
                        // numbers + decimal + comma (EU)
                        val filtered =
                            newValue.filter { it.isDigit() || it == '.' || it == ',' }
                        onChange(row.copy(qty = filtered))
                    },
                    label = { Text("Qty") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    modifier = Modifier.weight(0.4f)
                )

                SuggestionAutoCompleteField(
                    modifier = Modifier.weight(0.6f),
                    value = row.unit,
                    onValueChange = { onChange(row.copy(unit = it)) },
                    suggestions = suggestions.units,
                    label = "Unit",
                    showDropdownIcon = true,
                    matchMode = MatchMode.Contains
                )
            }
        }
        if (onRemove != null) {
            IconButton(
                modifier = Modifier.weight(0.2f),
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ingredient"
                )
            }
        }
    }
}

@Composable
fun StepFormField(
    s: String,
    suggestions: SuggestionsUi,
    idx: Int,
    onChange: (String) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = idx.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SuggestionAutoCompleteField(
            modifier = Modifier.weight(2f),
            value = s,
            onValueChange = {
                onChange(it)
            },
            suggestions = suggestions.steps,
            label = "Step $idx",
            showDropdownIcon = false,
            matchMode = MatchMode.Contains
        )

        if (onRemove != null) {
            IconButton(
                modifier = Modifier.weight(0.2f),
                onClick = onRemove
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove ingredient"
                )
            }
        }
    }
}

@Composable
fun RecipeForm(
    padding: PaddingValues,
    isLoading: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    pickedImageUri: String?,
    existingImageUrl: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    ingredients: List<IngredientFormRow>,
    onIngredientChange: (Int, IngredientFormRow) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    onIngredientAdd: () -> Unit,
    steps: List<String>,
    onStepChange: (Int, String) -> Unit,
    onStepRemove: (Int) -> Unit,
    onStepAdd: () -> Unit,
    suggestions: SuggestionsUi,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        if (isLoading) {
            item {
                Text("Loading…")
            }
        }

        item {
            RecipeEditFields(
                title = title,
                onTitleChange = onTitleChange,
                desc = desc,
                onDescChange = onDescChange,
            )
        }

        item {
            RecipeImagePicker(
                pickedImageUri = pickedImageUri,
                existingImageUrl = existingImageUrl,
                onPickClick = onPickImage,
                onRemoveClick = onRemoveImage
            )
        }

        item {
            SectionCard(title = "Ingredients") {
                if (ingredients.isEmpty()) {
                    Text(
                        "No ingredients added.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ingredients.forEachIndexed { idx, row ->
                        IngredientFormField(
                            index = idx + 1,
                            suggestions = suggestions,
                            row = row,
                            onChange = { onIngredientChange(idx, it) },
                            onRemove = { onIngredientRemove(idx) }
                        )

                        if (idx != ingredients.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            AddItemButton(
                text = "Add ingredient",
                onClick = onIngredientAdd
            )
        }

        item {
            SectionCard(title = "Steps") {
                if (steps.isEmpty()) {
                    Text(
                        "No steps added.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    steps.forEachIndexed { idx, step ->
                        StepFormField(
                            s = step,
                            suggestions = suggestions,
                            idx = idx + 1,
                            onChange = { onStepChange(idx, it) },
                            onRemove = { onStepRemove(idx) }
                        )

                        if (idx != steps.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        item {
            AddItemButton(
                text = "Add Step",
                onClick = onStepAdd
            )
        }
    }
}

@Preview(name = "Ingredient Item Preview", showBackground = true, widthDp = 360)
@Preview(
    name = "Ingredient Item Preview Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IngredientFormFieldPreview() {
    RecipeVaultTheme {
        IngredientFormField(
            index = 1,
            suggestions = SuggestionsUi(),
            row = IngredientFormRow(
                name = "Onion",
                qty = "2",
                unit = "medium"
            ),
            onChange = {},
            onRemove = {}
        )
    }
}

@Preview(name = "Step Item Row Preview", showBackground = true, widthDp = 360)
@Preview(
    name = "Step Item Row Preview Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StepFormFieldPreview() {
    RecipeVaultTheme {
        StepFormField(
            s = "Chop the onions finely.",
            suggestions = SuggestionsUi(),
            idx = 1,
            onChange = {},
            onRemove = {}
        )
    }
}


@Preview(name = "Recipe Form Preview", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(
    name = "Recipe Form Preview Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeFormPreview() {
    RecipeVaultTheme {
        RecipeForm(
            padding = PaddingValues(0.dp),
            isLoading = false,
            title = "Delicious Pasta",
            onTitleChange = {},
            desc = "A simple and delicious pasta recipe.",
            onDescChange = {},
            pickedImageUri = null,
            existingImageUrl = null,
            onPickImage = {},
            onRemoveImage = {},
            ingredients = listOf(
                IngredientFormRow("Pasta", "200", "grams"),
                IngredientFormRow("Tomato Sauce", "150", "ml"),
            ),
            onIngredientChange = { _, _ -> },
            onIngredientRemove = {},
            onIngredientAdd = {},
            steps = listOf(
                "Boil the pasta until al dente.",
                "Heat the tomato sauce in a pan."
            ),
            onStepChange = { _, _ -> },
            onStepRemove = {},
            onStepAdd = {},
            suggestions = SuggestionsUi(
                ingredients = listOf("Pasta", "Tomato Sauce", "Onion", "Garlic"),
                units = listOf("grams", "ml", "cups", "tablespoons"),
                steps = listOf("Chop the onions finely.", "Boil the pasta until al dente.")
            )
        )
    }
}

