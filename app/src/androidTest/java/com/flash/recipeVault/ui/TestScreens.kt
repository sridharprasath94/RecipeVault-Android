package com.flash.recipeVault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.vm.RecipeListViewModel

@Composable
fun RecipeListScreenForTest(
    repo: RecipeRepository
) {
    val vm = remember { RecipeListViewModel(repo) }
    val recipes by vm.recipes.collectAsState()

    androidx.compose.material3.Scaffold(
        topBar = { androidx.compose.material3.TopAppBar(title = { androidx.compose.material3.Text("Recipes") }) }
    ) { padding ->
        if (recipes.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier.padding(padding).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.material3.Text("No recipes yet. Tap + to add one.")
            }
        }
    }
}
