package com.flash.recipeVault.ui.screens.editRecipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeWithDetails
import com.flash.recipeVault.data.SuggestionType
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface EditRecipeEvent {
    data class Toast(val message: String) : EditRecipeEvent
    object OnFinishedSaving : EditRecipeEvent
}

data class EditRecipeUiState(
    val title: String = "",
    val description: String = "",
    val pickedImageUri: String? = null,
    val existingImageUrl: String? = null,
    val ingredients: List<IngredientFormRow> = listOf(IngredientFormRow()),
    val steps: List<String> = listOf(""),
    val isLoadingData: Boolean = false,
    val isSaving: Boolean = false,
)


class EditRecipeViewModel(
    container: AppContainer,
    internal val recipeId: Long,
) : ViewModel() {
    internal val recipeRepository = container.recipeRepositoryForCurrentUser()
    internal val suggestionsRepo = container.suggestionsRepositoryForCurrentUser()
    private val _ui = MutableStateFlow(EditRecipeUiState())
    val ui: StateFlow<EditRecipeUiState> = _ui
    val recipe = recipeRepository.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    private val _events = MutableSharedFlow<EditRecipeEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<EditRecipeEvent> = _events.asSharedFlow()

    init {
        _ui.update { it.copy(isLoadingData = true) }
        viewModelScope.launch {
            recipeRepository.observeRecipe(recipeId)
                .filterNotNull()
                .collect { recipe ->
                    _ui.value = mapRecipeToUi(recipe).copy(isLoadingData = false)
                }
        }
    }

    private fun mapRecipeToUi(r: RecipeWithDetails): EditRecipeUiState {
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

        return EditRecipeUiState(
            title = r.recipe.title,
            description = r.recipe.description.orEmpty(),
            ingredients = ingredientsUi,
            steps = stepsUi,
            pickedImageUri = null,
            existingImageUrl = r.recipe.imageUrl,
            isLoadingData = false
        )
    }

    fun updateDescription(description: String) {
        _ui.update { it.copy(description = description) }
    }

    fun updateTitle(title: String) {
        _ui.update { it.copy(title = title) }
    }

    fun onPickedImage(uri: String?) {
        _ui.update {
            it.copy(
                pickedImageUri = uri,
                // If user picked a new local image, ignore the previous remote image URL
                existingImageUrl = if (uri.isNullOrBlank()) it.existingImageUrl else null
            )
        }
    }

    fun onRemoveImage() {
        _ui.update { it.copy(pickedImageUri = null, existingImageUrl = null) }
    }

    fun onIngredientRemoved(index: Int) {
        _ui.update { state ->
            val list = state.ingredients.toMutableList()
            if (list.size <= 1) return@update state.copy(ingredients = listOf(IngredientFormRow()))
            if (index in list.indices) list.removeAt(index)
            state.copy(ingredients = list)
        }
    }

    fun onStepChanged(index: Int, value: String) {
        _ui.update { state ->
            val list = state.steps.toMutableList()
            if (index in list.indices) list[index] = value
            state.copy(steps = list)
        }
    }

    fun onAddStep() {
        _ui.update { it.copy(steps = it.steps + "") }
    }

    fun onStepRemoved(index: Int) {
        _ui.update { state ->
            val list = state.steps.toMutableList()
            if (list.size <= 1) return@update state.copy(steps = listOf(""))
            if (index in list.indices) list.removeAt(index)
            state.copy(steps = list)
        }
    }

    fun onAddIngredient() {
        _ui.update { it.copy(ingredients = it.ingredients + IngredientFormRow()) }
    }

    fun onIngredientChanged(index: Int, row: IngredientFormRow) {
        _ui.update { state ->
            val list = state.ingredients.toMutableList()
            list[index] = row
            state.copy(ingredients = list)
        }
    }

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


    fun save() {
        val state = ui.value
        val cleanTitle = state.title.trim()
        val cleanDesc = state.description.trim()

        // Normalize inputs (trim + drop empty rows)
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

        viewModelScope.launch {
            try {
                _ui.update { it.copy(isSaving = true) }

                recipeRepository.updateRecipe(
                    recipeId,
                    title = cleanTitle,
                    description = cleanDesc,
                    imageUri = state.pickedImageUri,
                    imageUrl = state.existingImageUrl,
                    ingredients = cleanIngredients,
                    steps = cleanSteps
                )

                suggestionsRepo.addFromTriples(
                    SuggestionType.INGREDIENT,
                    cleanIngredients.map { it.first }
                )

                suggestionsRepo.addFromTriples(
                    SuggestionType.UNIT,
                    cleanIngredients.mapNotNull { it.third }
                )

                onFinishedSaving()
            } catch (e: Exception) {
                toast(e.message ?: "Failed to save")
            } finally {
                _ui.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun onFinishedSaving() {
        _events.tryEmit(EditRecipeEvent.OnFinishedSaving)
    }

    private fun toast(message: String) {
        _events.tryEmit(EditRecipeEvent.Toast(message))
    }
}