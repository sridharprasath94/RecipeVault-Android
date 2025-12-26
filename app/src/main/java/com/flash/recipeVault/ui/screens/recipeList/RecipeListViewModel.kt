package com.flash.recipeVault.ui.screens.recipeList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecipeListUiState(
    val showMenu: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val deleteRecipeId: Long? = null,
    val isSyncing: Boolean = false,
    val isCloudSynced: Boolean = false,
) {
    val showDeleteDialog: Boolean get() = deleteRecipeId != null
}

sealed interface RecipeListEvent {
    object SyncNow : RecipeListEvent
    object BackupToDocument : RecipeListEvent
    object ShareBackup : RecipeListEvent
    object LoggedOut : RecipeListEvent

    data class Toast(val message: String) : RecipeListEvent
}

data class LastSyncedRead(
    val lastSyncedAt: Long,
    val shouldRemoveBadValue: Boolean
)

class RecipeListViewModel(
    private val repo: RecipeRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    val recipes = repo.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ui = MutableStateFlow(RecipeListUiState())
    val ui: StateFlow<RecipeListUiState> = _ui

    private val _events = MutableSharedFlow<RecipeListEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private val _lastSyncedAt = MutableStateFlow(0L)

    init {
        // Whenever local data changes, recompute whether we are still in-sync with the last successful cloud sync.
        viewModelScope.launch {
            recipes.collect {
                recomputeCloudSynced()
            }
        }
    }

    private fun recomputeCloudSynced() {
        val last = _lastSyncedAt.value
        val list = recipes.value
        // “Synced” means: nothing visible changed after the last successful sync.
        val synced = list.all { it.updatedAt <= last }
        _ui.update { it.copy(isCloudSynced = synced) }
    }

    fun openMenu() = _ui.update { it.copy(showMenu = true) }
    fun dismissMenu() = _ui.update { it.copy(showMenu = false) }

    fun requestLogout() = _ui.update { it.copy(showMenu = false, showLogoutDialog = true) }
    fun dismissLogout() = _ui.update { it.copy(showLogoutDialog = false) }

    fun confirmLogout() {
        _ui.update { it.copy(showLogoutDialog = false, showMenu = false) }
        auth.signOut()
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

    fun syncNowClicked() {
        dismissMenu()
        _ui.update { it.copy(isSyncing = true) }
        _events.tryEmit(RecipeListEvent.SyncNow)
    }

    fun backupClicked() {
        dismissMenu(); _events.tryEmit(RecipeListEvent.BackupToDocument)
    }

    fun shareClicked() {
        dismissMenu(); _events.tryEmit(RecipeListEvent.ShareBackup)
    }

    fun onSyncSucceeded(lastSyncedAt: Long) {
        _lastSyncedAt.value = lastSyncedAt
        _ui.update { it.copy(isSyncing = false) }
        recomputeCloudSynced()
    }

    fun onSyncFailed(message: String) {
        _ui.update { it.copy(isSyncing = false) }
        _events.tryEmit(RecipeListEvent.Toast(message))
        // keep previous isCloudSynced; it reflects last successful sync vs local changes
    }

    /** Restore persisted last successful sync timestamp (e.g., after app restart). */
    fun restoreLastSyncedAt(lastSyncedAt: Long) {
        _lastSyncedAt.value = lastSyncedAt
        _ui.update { it.copy(isSyncing = false) }
        recomputeCloudSynced()
    }


    fun parseLastSyncedAt(any: Any?): LastSyncedRead {
        val value = when (any) {
            is Long -> any
            is Int -> any.toLong()
            is String -> any.toLongOrNull() ?: 0L
            else -> 0L
        }

        val shouldRemove = any != null && any !is Long && any !is Int && any !is String
        return LastSyncedRead(lastSyncedAt = value, shouldRemoveBadValue = shouldRemove)
    }
}