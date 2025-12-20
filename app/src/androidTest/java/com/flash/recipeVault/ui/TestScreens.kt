package com.flash.recipeVault.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.vm.RecipeListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreenForTest(
    repo: RecipeRepository
) {
    val vm = remember { RecipeListViewModel(repo) }
    val recipes by vm.recipes.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipes") }) }
    ) { padding ->
        if (recipes.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text("No recipes yet. Tap + to add one.")
            }
        }
    }
}
