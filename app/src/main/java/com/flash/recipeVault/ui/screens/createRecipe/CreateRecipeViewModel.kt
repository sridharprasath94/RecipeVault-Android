package com.flash.recipeVault.ui.screens.createRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.ui.components.IngredientFormRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateRecipeUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
)
class CreateRecipeViewModel(
    private val repo: RecipeRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(CreateRecipeUiState())
    val ui: StateFlow<CreateRecipeUiState> = _ui
    private fun validate(
        title: String,
        ingredients: List<Triple<String, String?, String?>>,
        steps: List<String>
    ): String? {
        if (title.isBlank()) return "Title is required"

        // At least one ingredient must be fully filled (name + qty + unit)
        val hasAtLeastOneCompleteIngredient = ingredients.any { (name, qty, unit) ->
            name.isNotBlank() && !qty.isNullOrBlank() && !unit.isNullOrBlank()
        }
        if (!hasAtLeastOneCompleteIngredient) return "At least one ingredient is required"

        val hasAtLeastOneStep = steps.any { it.isNotBlank() }
        if (!hasAtLeastOneStep) return "At least one step is required"

        return null
    }

    fun save(
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl : String?,
        ingredients: List<IngredientFormRow>,
        steps: List<String>,
        onDone: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanTitle = title.trim()
        val cleanDesc = description?.trim()?.ifEmpty { null }

        // Normalize inputs (trim + drop empty rows)
        val cleanIngredients = ingredients
            .map { (n, q, u) ->
                Triple(
                    n.trim(),
                    q.trim().ifEmpty { null },
                    u.trim().ifEmpty { null }
                )
            }
            .filter { it.first.isNotEmpty() }

        val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }

        val error = validate(cleanTitle, cleanIngredients, cleanSteps)
        if (error != null) {
            onError(error)
            return
        }

        viewModelScope.launch {
            try {
                _ui.value = _ui.value.copy(isSaving = true, error = null)
                val id = repo.createRecipe(
                    title = cleanTitle,
                    description = cleanDesc,
                    imageUri = imageUri,
                    imageUrl = imageUrl,
                    ingredients = cleanIngredients,
                    steps = cleanSteps
                )
                onDone(id)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save recipe")
                _ui.value = _ui.value.copy(error = e.message ?: "Failed to save")
            }finally {
                _ui.value = _ui.value.copy(isSaving = false)
            }
        }
    }
}