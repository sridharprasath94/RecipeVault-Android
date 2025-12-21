@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.recipeList

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.compose.ui.tooling.preview.Preview
import com.flash.recipeVault.R
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.ConfirmationDialog
import com.flash.recipeVault.util.RecipeAsyncImage
import com.flash.recipeVault.ui.recipeList.RecipeListViewModel
import com.flash.recipeVault.data.RecipeEntity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    container: AppContainer,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val repo = remember { container.recipeRepositoryForCurrentUser() }
    val vm = remember { RecipeListViewModel(repo) }
    val recipes by vm.recipes.collectAsState()


    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Requires a real google-services.json to generate R.string.default_web_client_id
    val webClientId = stringResource(R.string.default_web_client_id)

    val googleClient = remember(webClientId) {
        GoogleSignIn.getClient(
            context,
            Builder(DEFAULT_SIGN_IN)
                .requestEmail()
                .apply { if (webClientId.isNotBlank()) requestIdToken(webClientId) }
                .build()
        )
    }

    var deleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }
    var deleteRecipeId by rememberSaveable { mutableStateOf<Long?>(null) }

    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showMenu by rememberSaveable { mutableStateOf(false) }

    // Save JSON to any document provider. If Google Drive is installed, choose Drive here.
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    val json = repo.exportAllAsJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    )

    RecipeListDialogs(
        showLogoutDialog = showLogoutDialog,
        onDismissLogout = { showLogoutDialog = false },
        onConfirmLogout = {
            showLogoutDialog = false
            vm.signOut()
            container.signOut()
            googleClient.signOut()
            onLogout()
        },
        showDeleteDialog = deleteConfirmationDialog,
        onDismissDelete = {
            deleteRecipeId = null
            deleteConfirmationDialog = false
        },
        onConfirmDelete = {
            deleteRecipeId?.let { vm.delete(it) }
            deleteRecipeId = null
            deleteConfirmationDialog = false
        }
    )

    Scaffold(
        topBar = {
            RecipeListTopBar(
                showMenu = showMenu,
                onOpenMenu = { showMenu = true },
                onDismissMenu = { showMenu = false },
                onSyncWithCloud = {
                    showMenu = false
                    scope.launch { container.firestoreSyncServiceForCurrentUser().syncNow() }
                },
                onBackup = {
                    showMenu = false
                    backupLauncher.launch("recipes_backup_${System.currentTimeMillis()}.json")
                },
                onShare = {
                    showMenu = false
                    scope.launch {
                        val json = repo.exportAllAsJson()
                        val file = File(
                            context.cacheDir,
                            "recipes_share_${System.currentTimeMillis()}.json"
                        )
                        file.writeText(json, Charsets.UTF_8)
                        val uri = FileProvider.getUriForFile(
                            context,
                            context.packageName + ".fileprovider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share backup"))
                    }
                },
                onLogoutClick = {
                    showMenu = false
                    showLogoutDialog = true
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        RecipeListBody(
            padding = padding,
            recipes = recipes,
            onOpen = onOpen,
            onDeleteClick = { id ->
                deleteRecipeId = id
                deleteConfirmationDialog = true
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListTopBar(
    showMenu: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onSyncWithCloud: () -> Unit,
    onBackup: () -> Unit,
    onShare: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Recipes") },
        actions = {
            IconButton(onClick = onOpenMenu) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu
            ) {
                DropdownMenuItem(
                    text = { Text("Sync with Cloud") },
                    onClick = onSyncWithCloud
                )

                DropdownMenuItem(
                    text = { Text("Backup") },
                    onClick = onBackup
                )

                DropdownMenuItem(
                    text = { Text("Share") },
                    onClick = onShare
                )

                DropdownMenuItem(
                    text = { Text("Log out") },
                    onClick = onLogoutClick
                )
            }
        }
    )
}

@Composable
fun RecipeListBody(
    padding: PaddingValues,
    recipes: List<RecipeEntity>,
    onOpen: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
) {
    if (recipes.isEmpty()) {
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No recipes yet. Tap + to add one.")
        }
    } else {
        RecipeList(
            padding = padding,
            recipes = recipes,
            onOpen = onOpen,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
fun RecipeList(
    padding: PaddingValues,
    recipes: List<RecipeEntity>,
    onOpen: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(recipes) { recipe ->
            RecipeListItem(
                recipe = recipe,
                onOpen = { onOpen(recipe.id) },
                onDeleteClick = { onDeleteClick(recipe.id) },
            )
        }
    }
}

@Composable
fun RecipeListItem(
    recipe: RecipeEntity,
    onOpen: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RecipeListItemText(
                title = recipe.title,
                description = recipe.description,
                onOpen = onOpen,
                modifier = Modifier.weight(1f),
            )

            recipe.imageUrl?.let { url ->
                RecipeAsyncImage(url)
            }

            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun RecipeListItemText(
    title: String,
    description: String?,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clickable(onClick = onOpen)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RecipeListDialogs(
    showLogoutDialog: Boolean,
    onDismissLogout: () -> Unit,
    onConfirmLogout: () -> Unit,
    showDeleteDialog: Boolean,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    ConfirmationDialog(
        show = showLogoutDialog,
        title = "Log out?",
        message = "Do you want to log out from this account?",
        confirmButtonText = "Log out",
        onConfirm = onConfirmLogout,
        onDismiss = onDismissLogout,
    )

    ConfirmationDialog(
        show = showDeleteDialog,
        title = "Delete recipe?",
        message = "This action cannot be undone.",
        confirmButtonText = "Delete",
        onConfirm = onConfirmDelete,
        onDismiss = onDismissDelete,
    )
}
