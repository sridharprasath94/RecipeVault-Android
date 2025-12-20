package com.flash.recipeVault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = row.name,
                onValueChange = { onChange(row.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )

            // Qty: numbers only
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
        }

        TextButton(onClick = onRemove) { Text("Remove") }
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

        // Click overlay so the whole field opens the menu
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