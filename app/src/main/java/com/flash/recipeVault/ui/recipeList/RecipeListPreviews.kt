package com.flash.recipeVault.ui.recipeList

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.data.RecipeEntity

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

@Preview(name = "TopBar - Menu Closed", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListTopBarPreview_Closed() {
    Scaffold(
        topBar = {
            RecipeListTopBar(
                showMenu = false,
                onOpenMenu = {},
                onDismissMenu = {},
                onSyncWithCloud = {},
                onBackup = {},
                onShare = {},
                onLogoutClick = {},
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}

@Preview(name = "TopBar - Menu Open", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListTopBarPreview_Open() {
    Scaffold(
        topBar = {
            RecipeListTopBar(
                showMenu = true,
                onOpenMenu = {},
                onDismissMenu = {},
                onSyncWithCloud = {},
                onBackup = {},
                onShare = {},
                onLogoutClick = {},
            )
        }
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}

@Preview(name = "List - Empty State", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeListBodyPreview_Empty() {
    RecipeListBody(
        padding = PaddingValues(0.dp),
        recipes = emptyList(),
        onOpen = {},
        onDeleteClick = {},
    )
}

@Preview(name = "List - With Items", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun RecipeListBodyPreview_List() {
    RecipeListBody(
        padding = PaddingValues(0.dp),
        recipes = previewRecipes(),
        onOpen = {},
        onDeleteClick = {},
    )
}

@Preview(name = "Item - With Description", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListItemPreview_WithDescription() {
    RecipeListItem(
        recipe = previewRecipes().first(),
        onOpen = {},
        onDeleteClick = {},
    )
}

@Preview(name = "Item - No Description", showBackground = true, widthDp = 360)
@Composable
private fun RecipeListItemPreview_NoDescription() {
    RecipeListItem(
        recipe = previewRecipes().last(),
        onOpen = {},
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