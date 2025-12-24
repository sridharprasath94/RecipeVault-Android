package com.flash.recipeVault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
fun IngredientRow(
    row: IngredientFormRow,
    onChange: (IngredientFormRow) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = row.name,
            onValueChange = { onChange(row.copy(name = it)) },
            label = { Text("Name") },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = row.qty,
            onValueChange = { new ->
                // allow only digits (0-9)
                if (new.all { it.isDigit() }) {
                    onChange(row.copy(qty = new))
                }
            },
            label = { Text("Qty") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(90.dp)
        )

        // Unit: dropdown
        UnitDropdown(
            value = row.unit,
            onSelected = { onChange(row.copy(unit = it)) },
            modifier = Modifier.width(130.dp)
        )

        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove ingredient",
            modifier = Modifier
                .clickable { onRemove() }
        )
    }
}

@Composable
private fun UnitDropdown(
    value: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select unit"
                )
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
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

@Composable
fun StepItemRow(
    s: String,
    idx: Int,
    onStepChange: (String) -> Unit,
    onStepsRemove: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = s,
            onValueChange = { onStepChange(it) },
            label = { Text("Step ${idx + 1}") },
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Remove ingredient",
            modifier = Modifier
                .clickable { onStepsRemove() }
        )
    }
}


@Preview(name = "Ingredient Row Preview", showBackground = true, widthDp = 360)
@Composable
private fun IngredientRowPreview() {
    RecipeVaultTheme {
        IngredientRow(
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
            onStepChange = {},
            onStepsRemove = {}
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


