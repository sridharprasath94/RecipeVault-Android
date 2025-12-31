@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.screens.recipeList

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.ui.components.ConfirmationDialog
import com.flash.recipeVault.util.RecipeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File


private const val cloudSyncedStatusKey = "cloud_synced"

private const val cloudLastSyncedTimeKey = "cloud_last_synced_at"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    vm: RecipeListViewModel,
    onAdd: () -> Unit,
    onOpenRecipe: (Long) -> Unit,
    onEditRecipe: (Long) -> Unit,
    onLoggedOut: () -> Unit
) {

    val context = LocalContext.current
    val ui by vm.ui.collectAsState()
    val scope = rememberCoroutineScope()
    val prefs = remember(ui.currentUserUid) {
        context.getSharedPreferences("recipe_list_sync_${ui.currentUserUid}", Context.MODE_PRIVATE)
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }

    LaunchedEffect(Unit) {
        val synced = prefs.getBoolean(cloudSyncedStatusKey, false)
        val last = prefs.getLong(cloudLastSyncedTimeKey, 0L)
        vm.restoreCloudStatus(synced, last)
    }

    LaunchedEffect(ui.recipes, ui.lastSyncedAt) {
        if (ui.lastSyncedAt <= 0L) return@LaunchedEffect
        val hasLocalNewer = ui.recipes.any { it.updatedAt > ui.lastSyncedAt }
        if (hasLocalNewer && ui.isCloudSynced) {
            prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
            vm.restoreCloudStatus(isCloudSynced = false, lastSyncedAt = ui.lastSyncedAt)
        }
    }

    LaunchedEffect(ui.isCloudSynced, ui.lastSyncedAt, ui.recipes) {
        vm.maybeAutoSync()
    }

    // Save JSON to any document provider. If Google Drive is installed, choose Drive here.
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            if (uri != null) {
                scope.launch {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(vm.exportedJson().toByteArray(Charsets.UTF_8))
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is RecipeListEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is RecipeListEvent.OnEditRecipe -> {
                    vm.startNavigation()
                    onEditRecipe(event.recipeId)
                }

                is RecipeListEvent.OnOpenRecipe -> {
                    vm.startNavigation()
                    onOpenRecipe(event.recipeId)
                }

                RecipeListEvent.SyncNow -> {
                    if (!ui.isSyncing) {
                        vm.requestSync(
                            onSuccess = { now ->
                                prefs.edit {
                                    putBoolean(cloudSyncedStatusKey, true)
                                        .putLong(cloudLastSyncedTimeKey, now)
                                }
                            },
                            onFailure = {
                                prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
                            }
                        )
                    }
                }

                RecipeListEvent.BackupToDocument -> {
                    backupLauncher.launch("recipes_backup_${System.currentTimeMillis()}.json")
                }

                RecipeListEvent.ShareBackup -> {
                    scope.launch {
                        val file = File(
                            context.cacheDir,
                            "recipes_share_${System.currentTimeMillis()}.json"
                        )
                        file.writeText(vm.exportedJson(), Charsets.UTF_8)
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

                RecipeListEvent.PerformGoogleSignOut -> {
                    val googleClient =
                        GoogleSignIn.getClient(
                            context, GoogleSignInOptions.Builder(
                                GoogleSignInOptions.DEFAULT_SIGN_IN
                            )
                                .requestEmail()
                                .build()
                        )
                    googleClient.signOut().addOnCompleteListener {
                        vm.onGoogleSignOutCompleted()
                    }
                }

                RecipeListEvent.LoggedOut -> {
                    vm.startNavigation()
                    onLoggedOut()
                }
            }
        }
    }

    RecipeListDialogs(
        showLogoutDialog = ui.showLogoutDialog,
        onDismissLogout = vm::dismissLogout,
        onConfirmLogout = vm::confirmLogout,
        showDeleteDialog = ui.showDeleteDialog,
        onDismissDelete = vm::dismissDelete,
        onConfirmDelete = vm::confirmDelete,
    )
    val isInteractionEnabled = !ui.isLoadingData && !ui.isNavigating
    Scaffold(
        topBar = {
            Box {
                TopAppBar(
                    title = { Text("Recipes") },
                    actions = {
                        if (isInteractionEnabled) {
                            IconButton(onClick = vm::onMenuToggle) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                            }

                            DropdownMenu(
                                expanded = ui.showMenu,
                                onDismissRequest = vm::onMenuDismiss
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ui.syncLabel)
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                ui.syncSupportingText,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    },
                                    onClick = vm::syncNowWithCloud,
                                    trailingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .padding(horizontal = 6.dp, vertical = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            SyncStatusIcon(
                                                isSyncing = ui.isSyncing,
                                                isCloudSynced = ui.isCloudSynced,
                                            )
                                        }
                                    }
                                )

                                DropdownMenuItem(
                                    text = { Text("Backup") },
                                    onClick = vm::backupClicked
                                )

                                DropdownMenuItem(
                                    text = { Text("Share") },
                                    onClick = vm::shareClicked
                                )

                                DropdownMenuItem(
                                    text = { Text("Log out") },
                                    onClick = vm::requestLogout
                                )
                            }
                        }
                    }

                )

                if (!isInteractionEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) { /* consume all touches */ }
                    )
                }
            }
        },
        floatingActionButton = {
            if (isInteractionEnabled) {
                FloatingActionButton(onClick = onAdd) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeListBody(
                padding = padding,
                recipes = ui.recipes,
                onOpen = vm::requestOpenRecipe,
                onEdit = vm::requestEditRecipe,
                onDeleteClick = vm::requestDelete
            )

            if (!isInteractionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                        )
                )
            }
        }
    }
}

@Composable
fun SyncStatusIcon(
    isSyncing: Boolean,
    isCloudSynced: Boolean,
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
                imageVector = Icons.Default.Check,
                contentDescription = "Synced",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        else -> {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "Not synced"
            )
        }
    }
}

@Composable
fun RecipeListBody(
    padding: PaddingValues,
    recipes: List<RecipeEntity>,
    onOpen: (Long) -> Unit,
    onEdit: (Long) -> Unit,
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
            onEdit = onEdit,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
fun RecipeList(
    padding: PaddingValues,
    recipes: List<RecipeEntity>,
    onOpen: (Long) -> Unit,
    onEdit: (Long) -> Unit,
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
                onEdit = { onEdit(recipe.id) },
                onDeleteClick = { onDeleteClick(recipe.id) },
            )
        }
    }
}

@Composable
fun RecipeListItem(
    recipe: RecipeEntity,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
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
                Box(
                    Modifier
                        .weight(1f)
                ) {
                    RecipeAsyncImage(url)
                }
            }


            IconButton(modifier = Modifier.weight(0.4f), onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }


            IconButton(modifier = Modifier.weight(0.4f), onClick = onDeleteClick) {
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
