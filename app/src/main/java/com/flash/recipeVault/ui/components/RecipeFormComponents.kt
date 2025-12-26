package com.flash.recipeVault.ui.components

import IngredientNameAutoComplete
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

data class IngredientFormRow(
    val name: String = "",
    val qty: String = "",
    val unit: String = ""
)

private val FoodUnits = listOf(
    "g",
    "kg",
    "ml",
    "l",
    "teaspoon",
    "tablespoon",
    "cup",
    "small",
    "medium",
    "large",
    "piece",
    "slice",
    "pinch"
)

@Composable
fun IngredientItem(
    index: Int,
    suggestions: List<String>,
    row: IngredientFormRow,
    onChange: (IngredientFormRow) -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    var ingredientName by remember {
        mutableStateOf(TextFieldValue(""))
    }

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
            IngredientNameAutoComplete(
                value = ingredientName,
                onValueChange = {
                    onChange(row.copy(name = it.text))
                    ingredientName = it
                },
                suggestions = suggestions
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
                UnitDropdown(
                    value = row.unit,
                    onSelected = { onChange(row.copy(unit = it)) },
                    modifier = Modifier.weight(0.6f)
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
fun StepItemRow(
    s: String,
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
        OutlinedTextField(
            modifier = Modifier.weight(2f),
            value = s,
            onValueChange = { onChange(it) },
            label = { Text("Step $idx") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(
    value: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = {
                Text(
                    "Unit",
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, !expanded)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            FoodUnits.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        expanded = false
                        onSelected(unit)
                    }
                )
            }
        }
    }
}


@Preview(name = "Ingredient Item Preview", showBackground = true, widthDp = 360)
@Composable
private fun IngredientItemPreview() {
    RecipeVaultTheme {
        IngredientItem(
            index = 1,
            suggestions = listOf("Onion", "Garlic", "Tomato"),
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
@Composable
private fun StepItemRowPreview() {
    RecipeVaultTheme {
        StepItemRow(
            s = "Chop the onions finely.",
            idx = 0,
            onChange = {},
            onRemove = {}
        )
    }
}

@Preview(name = "Unit Dropdown Preview", showBackground = true, widthDp = 360)
@Composable
private fun UnitDropdownPreview() {
    RecipeVaultTheme {
        UnitDropdown(
            value = "cup",
            onSelected = {}
        )
    }
}


