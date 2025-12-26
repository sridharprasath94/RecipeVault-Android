import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.PopupProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientNameAutoComplete(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val filtered = remember(value.text, suggestions) {
        if (suggestions.isEmpty()) emptyList()
        else if (value.text.isBlank()) suggestions.take(8)
        else suggestions
            .filter { it.contains(value.text, ignoreCase = true) }
            .take(8)
    }
    val showMenu = expanded && filtered.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = {
                Text(
                    "Ingredient"
                )
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, !expanded),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        )

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = false), // ✅ fixes “double backspace”
            modifier = Modifier.exposedDropdownSize(matchAnchorWidth = true)
        ) {
            filtered.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(
                            TextFieldValue(
                                text = item,
                                selection = TextRange(item.length)
                            )
                        )
                        expanded = false
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, widthDp = 360)
@Composable
private fun IngredientNameAutoCompletePreview() {
    var ingredientName by remember {
        mutableStateOf(TextFieldValue(""))
    }

    IngredientNameAutoComplete(
        value = ingredientName,
        onValueChange = { ingredientName = it },
        suggestions = listOf("Onion", "Garlic", "Tomato", "Salt", "Pepper"),
    )
}