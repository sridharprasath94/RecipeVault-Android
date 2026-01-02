package com.flash.recipeVault.ui.components


import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

@Composable
fun ConfirmationDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmButtonText: String,
    dismissButtonText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissButtonText)
            }
        }
    )
}

@Preview(name = "Confirmation Dialog Preview", showBackground = true, widthDp = 360)
@Preview(
    name = "Confirmation Dialog Preview - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ConfirmationDialogPreview() {
    RecipeVaultTheme {
        ConfirmationDialog(
            show = true,
            title = "Delete Recipe",
            message = "Are you sure you want to delete this recipe?",
            confirmButtonText = "Delete",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
