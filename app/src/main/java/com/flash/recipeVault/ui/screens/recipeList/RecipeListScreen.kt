@file:Suppress("DEPRECATION")

package com.flash.recipeVault.ui.screens.recipeList

import android.content.Context
import android.content.Intent
import android.util.Log
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.flash.recipeVault.ui.components.ConfirmationDialog
import com.flash.recipeVault.util.RecipeAsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
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
    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
    }
    val scope = rememberCoroutineScope()
    val prefs = remember(uid) {
        context.getSharedPreferences("recipe_list_sync_${uid}", Context.MODE_PRIVATE)
    }

    var didAutoSync by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val synced = prefs.getBoolean(cloudSyncedStatusKey, false)
        val last = prefs.getLong(cloudLastSyncedTimeKey, 0L)
        vm.restoreCloudStatus(synced, last)
    }

    LaunchedEffect(ui.rows, ui.lastSyncedAt) {
        if (ui.lastSyncedAt <= 0L) return@LaunchedEffect
        val hasLocalNewer = ui.rows.any { it.recipe.updatedAt > ui.lastSyncedAt }
        if (hasLocalNewer && ui.isCloudSynced) {
            prefs.edit { putBoolean(cloudSyncedStatusKey, false) }
            vm.restoreCloudStatus(isCloudSynced = false, lastSyncedAt = ui.lastSyncedAt)
        }
    }

    LaunchedEffect(ui.isCloudSynced, ui.lastSyncedAt, ui.rows) {
        if (didAutoSync) return@LaunchedEffect

        val shouldAutoSync =
            !ui.isCloudSynced && ui.lastSyncedAt == 0L && ui.rows.isNotEmpty()

        Log.d(
            "RecipeListScreen", "Auto-sync check: shouldAutoSync=$shouldAutoSync" +
                    " isCloudSynced=${ui.isCloudSynced} lastSyncedAt=${ui.lastSyncedAt} " +
                    "rowsCount=${ui.rows.size}"
        )
        if (shouldAutoSync) {
            didAutoSync = true
            vm.syncNowWithCloud()
        }
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
                is RecipeListEvent.Toast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_LONG
                ).show()

                is RecipeListEvent.OnEditRecipe -> onEditRecipe(event.recipeId)

                is RecipeListEvent.OnOpenRecipe -> onOpenRecipe(event.recipeId)

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

                RecipeListEvent.LoggedOut -> onLoggedOut()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipes") },
                actions = {
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
            rows = ui.rows,
            onOpen = vm::requestOpenRecipe,
            onEdit = vm::requestEditRecipe,
            onDeleteClick = vm::requestDelete
        )
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
    rows: List<RecipeListRowUi>,
    onOpen: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit,
) {
    if (rows.isEmpty()) {
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
            rows = rows,
            onOpen = onOpen,
            onEdit = onEdit,
            onDeleteClick = onDeleteClick,
        )
    }
}

@Composable
fun RecipeList(
    padding: PaddingValues,
    rows: List<RecipeListRowUi>,
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
        items(rows) { row ->
            RecipeListItem(
                row = row,
                onOpen = { onOpen(row.recipe.id) },
                onEdit = { onEdit(row.recipe.id) },
                onDeleteClick = { onDeleteClick(row.recipe.id) },
            )
        }
    }
}

@Composable
fun RecipeListItem(
    row: RecipeListRowUi,
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
                Text(row.recipe.title, style = MaterialTheme.typography.titleMedium)
                if (!row.recipe.description.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(row.recipe.description, style = MaterialTheme.typography.bodyMedium)
                }
            }


            row.recipe.imageUrl?.let { url ->
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
