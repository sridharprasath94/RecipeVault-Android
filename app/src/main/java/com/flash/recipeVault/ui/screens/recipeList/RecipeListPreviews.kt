package com.flash.recipeVault.ui.screens.recipeList

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

private fun previewRecipes(): List<RecipeEntity> = listOf(
    RecipeEntity(
        id = 1,
        title = "Pasta Arrabbiata",
        description = "Spicy tomato pasta with garlic and chili flakes.",
        imageUrl = "https://example.com/sample.jpg",
        createdAt = 0L,
        updatedAt = 0L,
    ),
    RecipeEntity(
        id = 2,
        title = "Masala Omelette",
        description = "Eggs with onion, chili, coriander. Quick breakfast.",
        // Using a sample URL for preview only; it may not render in Preview if network is disabled.
        imageUrl = "https://example.com/sample.jpg",
        createdAt = 0L,
        updatedAt = 0L,
    ),
    RecipeEntity(
        id = 3,
        title = "Overnight Oats",
        description = null,
        imageUrl = null,
        createdAt = 0L,
        updatedAt = 0L,
    ),
)

private fun fakeRecipeListUiState(
    recipes: List<RecipeEntity> = previewRecipes(),
    showMenu: Boolean = false,
    showLogoutDialog: Boolean = false,
    deleteRecipeId: Long? = null,
    isSyncing: Boolean = false,
    isCloudSynced: Boolean = true,
    lastSyncedAt: Long = System.currentTimeMillis(),
    isLoadingData: Boolean = false,
    isNavigating: Boolean = false,
): RecipeListUiState {
    return RecipeListUiState(
        recipes = recipes,
        showMenu = showMenu,
        showLogoutDialog = showLogoutDialog,
        deleteRecipeId = deleteRecipeId,
        isSyncing = isSyncing,
        isCloudSynced = isCloudSynced,
        lastSyncedAt = lastSyncedAt,
        isLoadingData = isLoadingData,
        isNavigating = isNavigating,
    )
}

@Preview(
    name = "Recipe List Content",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Preview(
    name = "Recipe List Content – Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeListContentPreview() {
    RecipeVaultTheme {
        RecipeListContent(
            ui = fakeRecipeListUiState(),
            onAdd = {},
            onMenuToggle = {},
            onMenuDismiss = {},
            onSyncNow = {},
            onBackup = {},
            onShare = {},
            onLogout = {},
            onOpenRecipe = {},
            onEditRecipe = {},
            onDeleteRecipe = {}
        )
    }
}

@Preview(
    name = "Recipe List – Menu Open",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Preview(
    name = "Recipe List – Menu Open - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeListMenuOpenPreview() {
    RecipeVaultTheme {
        RecipeListContent(
            ui = fakeRecipeListUiState(showMenu = true),
            onAdd = {},
            onMenuToggle = {},
            onMenuDismiss = {},
            onSyncNow = {},
            onBackup = {},
            onShare = {},
            onLogout = {},
            onOpenRecipe = {},
            onEditRecipe = {},
            onDeleteRecipe = {}
        )
    }
}

@Preview(
    name = "Recipe List – Loading",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Preview(
    name = "Recipe List – Loading - Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 720,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeListLoadingPreview() {
    RecipeVaultTheme {
        RecipeListContent(
            ui = fakeRecipeListUiState(
                isLoadingData = true
            ),
            onAdd = {},
            onMenuToggle = {},
            onMenuDismiss = {},
            onSyncNow = {},
            onBackup = {},
            onShare = {},
            onLogout = {},
            onOpenRecipe = {},
            onEditRecipe = {},
            onDeleteRecipe = {}
        )
    }
}


@Preview(name = "Dialog - Logout", showBackground = true, widthDp = 360)
@Preview(
    name = "Dialog - Logout - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeListDialogsLogoutPreview() {
    RecipeVaultTheme {
        RecipeListDialogs(
            showLogoutDialog = true,
            onDismissLogout = {},
            onConfirmLogout = {},
            showDeleteDialog = false,
            onDismissDelete = {},
            onConfirmDelete = {},
        )
    }
}

@Preview(name = "Dialog - Delete", showBackground = true, widthDp = 360)
@Preview(
    name = "Dialog - Delete - Dark",
    showBackground = true,
    widthDp = 360,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun RecipeListDialogsDeletePreview() {
    RecipeVaultTheme {
        RecipeListDialogs(
            showLogoutDialog = false,
            onDismissLogout = {},
            onConfirmLogout = {},
            showDeleteDialog = true,
            onDismissDelete = {},
            onConfirmDelete = {},
        )
    }
}