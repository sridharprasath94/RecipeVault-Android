package com.flash.recipeVault.ui.editRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.ui.createRecipe.CreateRecipeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class EditRecipeUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
)

class EditRecipeViewModel(
    private val recipeId: Long,
    private val repo: RecipeRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(EditRecipeUiState())
    val ui: StateFlow<EditRecipeUiState> = _ui
    val recipe = repo.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), null)

    fun save(
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl: String?,
        ingredients: List<Triple<String, String?, String?>>,
        steps: List<String>,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isSaving = true, error = null)
                repo.updateRecipe(
                    recipeId,
                    title,
                    description,
                    imageUri,
                    imageUrl,
                    ingredients,
                    steps
                )
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
                _ui.value = _ui.value.copy(error = e.message ?: "Failed to save")
            } finally {
                _ui.value = _ui.value.copy(isSaving = false)
            }
        }
    }
}