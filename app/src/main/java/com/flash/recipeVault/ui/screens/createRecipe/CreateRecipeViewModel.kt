package com.flash.recipeVault.ui.screens.createRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.SuggestionType
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import com.flash.recipeVault.ui.screens.recipeDetail.RecipeDetailEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CreateRecipeEvent {
    data class Toast(val message: String) : CreateRecipeEvent
    object OnBackClicked : CreateRecipeEvent
    data class OnFinishedSaving(val id: Long) : CreateRecipeEvent
}

data class CreateRecipeUiState(
    val title: String = "",
    val description: String = "",
    val pickedImageUri: String? = null,
    val existingImageUrl: String? = null,
    val ingredients: List<IngredientFormRow> = listOf(IngredientFormRow()),
    val steps: List<String> = listOf(""),
    val isSaving: Boolean = false,
)

class CreateRecipeViewModel(
    container: AppContainer,
) : ViewModel() {

    private val recipeRepository = container.recipeRepositoryForCurrentUser()
    private val suggestionsRepo = container.suggestionsRepositoryForCurrentUser()
    private val _ui = MutableStateFlow(CreateRecipeUiState())
    val ui: StateFlow<CreateRecipeUiState> = _ui
    private val _events = MutableSharedFlow<CreateRecipeEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<CreateRecipeEvent> = _events.asSharedFlow()

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

    fun updateTitle(value: String) {
        _ui.update { it.copy(title = value) }
    }

    fun updateDescription(value: String) {
        _ui.update { it.copy(description = value) }
    }

    fun onPickedImage(uri: String?) {
        _ui.update {
            it.copy(
                pickedImageUri = uri,
                // Prefer local picked image over any existing URL
                existingImageUrl = if (!uri.isNullOrBlank()) null else it.existingImageUrl
            )
        }
    }

    fun onRemoveImage() {
        _ui.update { it.copy(pickedImageUri = null, existingImageUrl = null) }
    }

    fun onAddIngredient() {
        _ui.update { it.copy(ingredients = it.ingredients + IngredientFormRow()) }
    }

    fun onIngredientChanged(index: Int, row: IngredientFormRow) {
        _ui.update { state ->
            val list = state.ingredients.toMutableList()
            if (index in list.indices) list[index] = row
            state.copy(ingredients = list)
        }
    }

    fun onIngredientRemoved(index: Int) {
        _ui.update { state ->
            val list = state.ingredients.toMutableList()
            if (list.size <= 1) {
                return@update state.copy(ingredients = listOf(IngredientFormRow()))
            }
            if (index in list.indices) list.removeAt(index)
            state.copy(ingredients = list)
        }
    }

    fun onAddStep() {
        _ui.update { it.copy(steps = it.steps + "") }
    }

    fun onStepChanged(index: Int, value: String) {
        _ui.update { state ->
            val list = state.steps.toMutableList()
            if (index in list.indices) list[index] = value
            state.copy(steps = list)
        }
    }

    fun onStepRemoved(index: Int) {
        _ui.update { state ->
            val list = state.steps.toMutableList()
            if (list.size <= 1) {
                return@update state.copy(steps = listOf(""))
            }
            if (index in list.indices) list.removeAt(index)
            state.copy(steps = list)
        }
    }

    fun requestBack() = _events.tryEmit(CreateRecipeEvent.OnBackClicked)

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

    fun save() {
        val state = _ui.value
        val cleanTitle = state.title.trim()
        val cleanDesc = state.description.trim().ifEmpty { null }

        val cleanIngredients = state.ingredients
            .map { (n, q, u) ->
                Triple(
                    n.trim(),
                    q.trim().ifEmpty { null },
                    u.trim().ifEmpty { null }
                )
            }
            .filter { it.first.isNotEmpty() }

        val cleanSteps = state.steps.map { it.trim() }.filter { it.isNotEmpty() }

        val error = validate(cleanTitle, cleanIngredients, cleanSteps)
        if (error != null) {
            toast(error)
            return
        }

        if (_ui.value.isSaving) return
        viewModelScope.launch {
            try {
                _ui.update { it.copy(isSaving = true) }
                val id = recipeRepository.createRecipe(
                    title = cleanTitle,
                    description = cleanDesc,
                    imageUri = state.pickedImageUri,
                    imageUrl = state.existingImageUrl,
                    ingredients = cleanIngredients,
                    steps = cleanSteps
                )

                suggestionsRepo.addFromTriples(
                    type = SuggestionType.INGREDIENT,
                    values = cleanIngredients.map { it.first }
                )
                suggestionsRepo.addFromTriples(
                    type = SuggestionType.UNIT,
                    values = cleanIngredients.mapNotNull { it.third }
                )
                suggestionsRepo.addFromTriples(
                    type = SuggestionType.STEP,
                    values = cleanSteps
                )
                onFinishedSaving(id)
            } catch (e: Exception) {
                toast(e.message ?: "Failed to save")
            } finally {
                _ui.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun onFinishedSaving(id: Long) {
        _events.tryEmit(CreateRecipeEvent.OnFinishedSaving(id))
    }

    private fun toast(message: String) {
        _events.tryEmit(CreateRecipeEvent.Toast(message))
    }

}