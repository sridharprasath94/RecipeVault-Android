package com.flash.recipeVault.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditRecipeViewModel(
    private val recipeId: Long,
    private val repo: RecipeRepository
) : ViewModel() {

    val recipe = repo.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(
        title: String,
        description: String?,
        imageUri: String?,
        imageUrl : String?,
        ingredients: List<Triple<String, String?, String?>>,
        steps: List<String>,
        onDone: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repo.updateRecipe(recipeId, title, description, imageUri, imageUrl, ingredients, steps)
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}
