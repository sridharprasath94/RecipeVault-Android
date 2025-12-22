package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeDetailViewModel(
    private val recipeId: Long,
    private val repo: RecipeRepository
) : ViewModel() {

    val recipe = repo.observeRecipe(recipeId)
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), null)

    fun delete() = viewModelScope.launch { repo.deleteRecipe(recipeId) }
}