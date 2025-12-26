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
        _ui.update { it.copy(deleteRecipeId = null, isCloudSynced = false) }
        viewModelScope.launch { repo.deleteRecipe(id) }
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

    fun onSyncSucceeded() {
        _ui.update { it.copy(isSyncing = false, isCloudSynced = true) }
    }

    fun onSyncFailed(message: String) {
        _ui.update { it.copy(isSyncing = false, isCloudSynced = false) }
        _events.tryEmit(RecipeListEvent.Toast(message))
    }

    /** Restore persisted cloud-sync status (e.g., after app restart). */
    fun restoreCloudSynced(isSynced: Boolean) {
        _ui.update { it.copy(isCloudSynced = isSynced, isSyncing = false) }
    }
}