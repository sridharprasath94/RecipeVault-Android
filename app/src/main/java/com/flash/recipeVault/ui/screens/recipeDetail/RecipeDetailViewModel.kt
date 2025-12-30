package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.di.AppContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RecipeDetailUiState(
    val deleteRecipeId: Long? = null,
) {
    val showDeleteDialog: Boolean get() = deleteRecipeId != null
}

sealed interface RecipeDetailEvent {
    data class Toast(val message: String) : RecipeDetailEvent
    object Deleted : RecipeDetailEvent
    object OnBackClicked : RecipeDetailEvent
    data class OnEditClicked(val recipeId: Long) : RecipeDetailEvent
}

class RecipeDetailViewModel(
    container: AppContainer,
    private val recipeId: Long,
) : ViewModel() {
    private val repo = container.recipeRepositoryForCurrentUser()
    val recipe = repo.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _ui = MutableStateFlow(RecipeDetailUiState())
    val ui: StateFlow<RecipeDetailUiState> = _ui
    private val _events = MutableSharedFlow<RecipeDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun requestBack() = _events.tryEmit(RecipeDetailEvent.OnBackClicked)
    fun requestEdit() = _events.tryEmit(RecipeDetailEvent.OnEditClicked(recipeId))
    fun requestDelete() = _ui.update { it.copy(deleteRecipeId = recipeId) }
    fun dismissDelete() = _ui.update { it.copy(deleteRecipeId = null) }

    fun confirmDelete() {
        val id = _ui.value.deleteRecipeId ?: return
        _ui.update { it.copy(deleteRecipeId = null) }

        viewModelScope.launch {
            runCatching {
                repo.deleteRecipe(id)
            }.onSuccess {
                _events.emit(RecipeDetailEvent.Toast("Recipe deleted"))
                _events.emit(RecipeDetailEvent.Deleted)
            }.onFailure {
                _events.emit(
                    RecipeDetailEvent.Toast(
                        it.message ?: "Failed to delete recipe"
                    )
                )
            }
        }
    }
}