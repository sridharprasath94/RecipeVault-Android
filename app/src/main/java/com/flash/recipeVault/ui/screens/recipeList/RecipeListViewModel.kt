package com.flash.recipeVault.ui.screens.recipeList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.recipeVault.data.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecipeListViewModel(private val repo: RecipeRepository,
                          private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : ViewModel() {
    val recipes = repo.observeRecipes()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) = viewModelScope.launch { repo.deleteRecipe(id) }

    fun signOut() {
        auth.signOut()
    }
}