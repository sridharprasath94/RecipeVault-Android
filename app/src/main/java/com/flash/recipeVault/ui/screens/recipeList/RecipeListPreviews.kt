package com.flash.recipeVault.ui.screens.recipeList

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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


@Preview(name = "List - Empty State", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeListBodyPreview_Empty() {
    RecipeListBody(
        padding = PaddingValues(0.dp),
        recipes = emptyList(),
        onOpen = {},
        onEdit = {},
        onDeleteClick = {},
    )
}

@Preview(name = "List - With Items", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeListBodyPreview_List() {
    RecipeListBody(
        padding = PaddingValues(0.dp),
        recipes = emptyList(),
        onOpen = {},
        onEdit = {},
        onDeleteClick = {},
    )
}

@Preview(name = "Item - With Description", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListItemPreview_WithDescription() {
    RecipeListItem(
        recipe = previewRecipes().first(),
        onOpen = {},
        onEdit = {},
        onDeleteClick = {},
    )
}

@Preview(name = "Item - No Description", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListItemPreview_NoDescription() {
    RecipeListItem(
        recipe = previewRecipes().first(),
        onOpen = {},
        onEdit = {},
        onDeleteClick = {},
    )
}

@Preview(name = "Dialog - Logout", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListDialogsPreview_Logout() {
    RecipeListDialogs(
        showLogoutDialog = true,
        onDismissLogout = {},
        onConfirmLogout = {},
        showDeleteDialog = false,
        onDismissDelete = {},
        onConfirmDelete = {},
    )
}

@Preview(name = "Dialog - Delete", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListDialogsPreview_Delete() {
    RecipeListDialogs(
        showLogoutDialog = false,
        onDismissLogout = {},
        onConfirmLogout = {},
        showDeleteDialog = true,
        onDismissDelete = {},
        onConfirmDelete = {},
    )
}

@Preview(
    name = "Recipe List – Normal",
    showBackground = true,
    widthDp = 360,
    heightDp = 720
)
@Composable
private fun RecipeListPreview_Normal() {
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
@Composable
private fun RecipeListPreview_MenuOpen() {
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
@Composable
private fun RecipeListPreview_Loading() {
    RecipeVaultTheme {
        RecipeListContent(
            ui = fakeRecipeListUiState(
                recipes = emptyList(),
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