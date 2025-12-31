package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeWithDetails
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.screens.recipeList.RecipeListEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class RecipeDetailUiState(
    val title: String = "",
    val description: String = "",
    val pickedImageUri: String? = null,
    val existingImageUrl: String? = null,
    val ingredients: List<IngredientFormRow> = listOf(IngredientFormRow()),
    val steps: List<String> = listOf(""),
    val isLoadingData: Boolean = false,
    val deleteRecipeId: Long? = null,
    val isNavigating: Boolean = false,
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
    private val _ui = MutableStateFlow(RecipeDetailUiState())
    val ui: StateFlow<RecipeDetailUiState> = _ui
    private val _events = MutableSharedFlow<RecipeDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init {
        _ui.update { it.copy(isLoadingData = true) }
        viewModelScope.launch {
            val recipe = repo.observeRecipe(recipeId)
                .filterNotNull()
                .first()
            _ui.value = mapRecipeToUi(recipe).copy(isLoadingData = false)
        }
    }

    private fun mapRecipeToUi(r: RecipeWithDetails): RecipeDetailUiState {
        val ingredientsUi = r.ingredients
            .sortedBy { it.sortOrder }
            .map {
                IngredientFormRow(
                    name = it.name,
                    qty = it.quantity.orEmpty(),
                    unit = it.unit.orEmpty()
                )
            }
            .ifEmpty { listOf(IngredientFormRow()) }

        val stepsUi =
            r.steps
                .sortedBy { it.sortOrder }
                .map { it.instruction }
                .ifEmpty { listOf("") }

        return RecipeDetailUiState(
            title = r.recipe.title,
            description = r.recipe.description.orEmpty(),
            ingredients = ingredientsUi,
            steps = stepsUi,
            pickedImageUri = null,
            existingImageUrl = r.recipe.imageUrl,
            isLoadingData = false
        )
    }

    fun requestBack() = emitIfAllowed(RecipeDetailEvent.OnBackClicked)
    fun requestEdit() = emitIfAllowed(RecipeDetailEvent.OnEditClicked(recipeId))
    fun requestDelete() = _ui.update { it.copy(deleteRecipeId = recipeId) }
    fun dismissDelete() = _ui.update { it.copy(deleteRecipeId = null) }

    fun confirmDelete() {
        val id = _ui.value.deleteRecipeId ?: return
        _ui.update { it.copy(deleteRecipeId = null) }

        viewModelScope.launch {
            runCatching {
                repo.deleteRecipe(id)
            }.onSuccess {
                emitIfAllowed(RecipeDetailEvent.Toast("Recipe deleted"))
                emitIfAllowed(RecipeDetailEvent.Deleted)
            }.onFailure {
                emitIfAllowed(
                    RecipeDetailEvent.Toast(
                        it.message ?: "Failed to delete recipe"
                    )
                )
            }
        }
    }

    fun startNavigation() {
        _ui.update { it.copy(isNavigating = true) }
    }

    fun onScreenVisible() {
        _ui.update { it.copy(isNavigating = false) }
    }

    private fun emitIfAllowed(event: RecipeDetailEvent) {
        if (!_ui.value.isNavigating) {
            _events.tryEmit(event)
        }
    }
}