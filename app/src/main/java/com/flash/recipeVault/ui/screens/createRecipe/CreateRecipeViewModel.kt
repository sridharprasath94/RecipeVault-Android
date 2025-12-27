package com.flash.recipeVault.ui.screens.createRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.SuggestionsRepository
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.data.SuggestionType
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CreateRecipeUiState(
    val isSaving: Boolean = false,
    val error: String? = null,
)

class CreateRecipeViewModel(
    private val repo: RecipeRepository,
    private val suggestionsRepo: SuggestionsRepository,
) : ViewModel() {
    private val _ui = MutableStateFlow(CreateRecipeUiState())
    val ui: StateFlow<CreateRecipeUiState> = _ui

    val suggestions: StateFlow<SuggestionsUi> =
        combine(
            suggestionsRepo.observeAllMerged(SuggestionType.INGREDIENT),
            suggestionsRepo.observeAllMerged(SuggestionType.UNIT),
            suggestionsRepo.observeAllMerged(SuggestionType.STEP),
        ) { ing, unit, step ->
            SuggestionsUi(
                ingredients = ing,
                units = unit,
                steps = step
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SuggestionsUi()
        )

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

    fun clearError() {
        if (_ui.value.error != null) {
            _ui.value = _ui.value.copy(error = null)
        }
    }

    fun save(
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl: String?,
        ingredients: List<IngredientFormRow>,
        steps: List<String>,
        onDone: (Long) -> Unit,
    ) {
        val cleanTitle = title.trim()
        val cleanDesc = description?.trim()?.ifEmpty { null }

        val cleanIngredients = ingredients
            .map { (n, q, u) ->
                Triple(
                    n.trim(),
                    q.trim(),
                    u.trim()
                )
            }
            .filter { it.first.isNotEmpty() }

        val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }

        val error = validate(cleanTitle, cleanIngredients, cleanSteps)
        if (error != null) {
            _ui.value = _ui.value.copy(error = error)
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

                suggestionsRepo.addFromTriples(
                    SuggestionType.INGREDIENT,
                    cleanIngredients.map { it.first }
                )

                suggestionsRepo.addFromTriples(
                    SuggestionType.UNIT,
                    cleanIngredients.map { it.third }
                )

                onDone(id)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(error = e.message ?: "Failed to save")
            } finally {
                _ui.value = _ui.value.copy(isSaving = false)
            }
        }
    }
}