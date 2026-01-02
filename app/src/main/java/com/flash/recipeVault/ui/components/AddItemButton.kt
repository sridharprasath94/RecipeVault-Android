package com.flash.recipeVault.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

@Composable
fun AddItemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Preview(name = "Add Item Button Preview", showBackground = true, widthDp = 360)
@Preview(
    name = "Add Item Button Preview - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AddItemButtonPreview() {
    RecipeVaultTheme {
        AddItemButton(
            text = "Add Ingredient",
            onClick = {}
        )
    }
}

@Preview(name = "Add Item Button Long Text Preview", showBackground = true, widthDp = 360)
@Preview(
    name = "Add Item Button Long Text Preview - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AddItemButtonLongTextPreview() {
    RecipeVaultTheme {
        AddItemButton(
            text = "Add Another Ingredient to the List",
            onClick = {}
        )
    }
}