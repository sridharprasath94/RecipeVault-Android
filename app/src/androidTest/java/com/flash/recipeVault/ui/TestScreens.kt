package com.flash.recipeVault.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.test.core.app.ApplicationProvider
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.screens.recipeList.RecipeListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreenForTest(
) {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val vm = remember { RecipeListViewModel(container = AppContainer(context)) }
    val ui = vm.ui.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recipes") }) }
    ) { padding ->
        if (ui.value.recipes.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No recipes yet. Tap + to add one.")
            }
        }
    }
}
