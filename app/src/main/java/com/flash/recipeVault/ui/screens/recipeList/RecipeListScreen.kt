@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.screens.recipeList

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.flash.recipeVault.R
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.ConfirmationDialog
import com.flash.recipeVault.util.RecipeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.edit

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
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous" }
    val syncPrefs = remember(uid) {
        context.getSharedPreferences("recipe_list_sync_${uid}", Context.MODE_PRIVATE)
    }
    val prefsKey = remember { "last_synced_at" }

    LaunchedEffect(uid) {
        val parsed = vm.parseLastSyncedAt(syncPrefs.all[prefsKey])
        if (parsed.shouldRemoveBadValue) {
            syncPrefs.edit { remove(prefsKey) }
        }
        vm.restoreLastSyncedAt(parsed.lastSyncedAt)
    }

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

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                RecipeListEvent.SyncNow -> {
                    runCatching {
                        container.firestoreSyncServiceForCurrentUser().syncNow()
                    }.onSuccess {
                        val now = System.currentTimeMillis()
                        syncPrefs.edit { putLong(prefsKey, now) }
                        vm.onSyncSucceeded(now)
                    }.onFailure {
                        vm.onSyncFailed(it.message ?: "Sync failed")
                    }
                }

                RecipeListEvent.BackupToDocument -> {
                    backupLauncher.launch("recipes_backup_${System.currentTimeMillis()}.json")
                }

                RecipeListEvent.ShareBackup -> {
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
                }

                RecipeListEvent.LoggedOut -> {
                    container.signOut()
                    googleClient.signOut()
                    onLogout()
                }

                is RecipeListEvent.Toast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }
    }

    RecipeListDialogs(
        showLogoutDialog = ui.showLogoutDialog,
        onDismissLogout = vm::dismissLogout,
        onConfirmLogout = {
            vm.confirmLogout()
        },
        showDeleteDialog = ui.showDeleteDialog,
        onDismissDelete = vm::dismissDelete,
        onConfirmDelete = vm::confirmDelete,
    )

    Scaffold(
        topBar = {
            RecipeListTopBar(
                showMenu = ui.showMenu,
                isCloudSynced = ui.isCloudSynced,
                isSyncing = ui.isSyncing,
                onOpenMenu = vm::openMenu,
                onDismissMenu = vm::dismissMenu,
                onSyncWithCloud = vm::syncNowClicked,
                onBackup = vm::backupClicked,
                onShare = vm::shareClicked,
                onLogoutClick = vm::requestLogout,
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
            onDeleteClick = { id -> vm.requestDelete(id) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListTopBar(
    showMenu: Boolean,
    isCloudSynced: Boolean,
    isSyncing: Boolean,
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
                    text = {
                        val label = when {
                            isSyncing -> "Syncing with Cloud…"
                            isCloudSynced -> "Cloud Synced"
                            else -> "Sync with Cloud"
                        }
                        Text(label)
                    },
                    onClick = onSyncWithCloud,
                    trailingIcon = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            tonalElevation = 1.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isSyncing -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }

                                    isCloudSynced -> {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Synced",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
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
                .padding(12.dp)
                .clickable(onClick = onOpen),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                Modifier
                    .weight(1f)
            ) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium)
                if (!recipe.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

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
