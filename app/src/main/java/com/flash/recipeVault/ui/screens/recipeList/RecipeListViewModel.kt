package com.flash.recipeVault.ui.screens.recipeList

import android.text.format.DateFormat
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeEntity
import com.flash.recipeVault.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeListRowUi(
    val recipe: RecipeEntity,
)

data class RecipeListUiState(
    val rows: List<RecipeListRowUi> = emptyList(),
    val showMenu: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val deleteRecipeId: Long? = null,
    val isSyncing: Boolean = false,
    val isCloudSynced: Boolean = false,
    val lastSyncedAt: Long = 0L,
) {
    val showDeleteDialog: Boolean get() = deleteRecipeId != null
    val syncLabel: String
        get() = when {
            isSyncing -> "Syncing…"
            isCloudSynced -> "Cloud Synced"
            else -> "Sync now"
        }
    val syncSupportingText: String
        get() = if (lastSyncedAt > 0L) {
            val dt = DateFormat.format("dd MMM, HH:mm", lastSyncedAt).toString()
            "Last synced: $dt"
        } else {
            "Not synced yet"
        }
}

sealed interface RecipeListEvent {
    object SyncNow : RecipeListEvent
    object BackupToDocument : RecipeListEvent
    object ShareBackup : RecipeListEvent
    object PerformGoogleSignOut : RecipeListEvent
    object LoggedOut : RecipeListEvent
    data class Toast(val message: String) : RecipeListEvent
}

class RecipeListViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val repo = container.recipeRepositoryForCurrentUser()
    val recipes = repo.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ui = MutableStateFlow(RecipeListUiState())
    val ui: StateFlow<RecipeListUiState> = _ui

    private val _events = MutableSharedFlow<RecipeListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            recipes.collect { list ->
                val rows = list.map {
                    RecipeListRowUi(recipe = it)
                }
                _ui.update { it.copy(rows = rows) }
            }
        }
    }

    fun syncNowWithCloud() {
        _events.tryEmit(RecipeListEvent.SyncNow)
    }

    fun backupClicked() {
        onMenuDismiss(); _events.tryEmit(RecipeListEvent.BackupToDocument)
    }

    fun shareClicked() {
        onMenuDismiss(); _events.tryEmit(RecipeListEvent.ShareBackup)
    }

    /** Restore persisted last successful sync timestamp (e.g., after app restart). */
    fun restoreCloudStatus(isCloudSynced: Boolean, lastSyncedAt: Long) {
        val validSynced = isCloudSynced && lastSyncedAt > 0L

        _ui.update {
            it.copy(
                isCloudSynced = validSynced,
                lastSyncedAt = lastSyncedAt
            )
        }
    }

    fun onMenuToggle() = _ui.update { it.copy(showMenu = !it.showMenu) }
    fun onMenuDismiss() = _ui.update { it.copy(showMenu = false) }


    fun requestLogout() = _ui.update { it.copy(showMenu = false, showLogoutDialog = true) }
    fun dismissLogout() = _ui.update { it.copy(showLogoutDialog = false) }

    fun confirmLogout() {
        _ui.update { it.copy(showLogoutDialog = false, showMenu = false) }
        _events.tryEmit(RecipeListEvent.PerformGoogleSignOut)
    }

    fun onGoogleSignOutCompleted() {
        container.signOut()
        _events.tryEmit(RecipeListEvent.LoggedOut)
    }

    fun requestDelete(id: Long) = _ui.update { it.copy(deleteRecipeId = id) }
    fun dismissDelete() = _ui.update { it.copy(deleteRecipeId = null) }

    fun confirmDelete() {
        val id = _ui.value.deleteRecipeId ?: return
        _ui.update { it.copy(deleteRecipeId = null) }
        viewModelScope.launch {
            repo.deleteRecipe(id)
            _events.tryEmit(RecipeListEvent.SyncNow)
        }
    }

    fun requestSync(onSuccess: (Long) -> Unit, onFailure: (String) -> Unit) {
        if (_ui.value.isSyncing) return
        _ui.update { it.copy(isSyncing = true, showMenu = false) }

        viewModelScope.launch {
            runCatching {
                container.firestoreSyncServiceForCurrentUser().syncNow()
            }.onSuccess {
                val now = System.currentTimeMillis()
                _ui.update { it.copy(isSyncing = false, isCloudSynced = true, lastSyncedAt = now) }
                onSuccess(now)
            }.onFailure { it ->
                _ui.update { it.copy(isSyncing = false, isCloudSynced = false) }
                val msg = it.message ?: "Sync failed"
                _events.tryEmit(RecipeListEvent.Toast(msg))
                onFailure(msg)
            }
        }
    }

    suspend fun exportedJson(): String {
        return repo.exportAllAsJson()
    }
}