package com.flash.recipeVault.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.util.rememberBase64ImageBitmap
import com.flash.recipeVault.vm.RecipeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    container: AppContainer,
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val repo = remember { container.recipeRepositoryForCurrentUser() }
    val vm = remember { RecipeDetailViewModel(recipeId, repo) }
    val recipeWithDetails by vm.recipe.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    TextButton(onClick = { onEdit(recipeId) }) { Text("Edit") }
                    IconButton(onClick = { vm.delete(); onBack() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        val data = recipeWithDetails
        if (data == null) {
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text("Loading…")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(data.recipe.title, style = MaterialTheme.typography.headlineSmall)
                    if (!data.recipe.description.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(data.recipe.description, style = MaterialTheme.typography.bodyLarge)
                    }
                    val imageBitmap = rememberBase64ImageBitmap(data.recipe.imageUrl)

                    imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Recipe image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }

                item { HorizontalDivider() }

                item { Text("Ingredients", style = MaterialTheme.typography.titleMedium) }
                items(data.ingredients.sortedBy { it.sortOrder }) { ing ->
                    val suffix = listOfNotNull(ing.quantity, ing.unit).joinToString(" ")
                    val text = if (suffix.isNotBlank()) "• ${ing.name} $suffix" else "• ${ing.name}"
                    Text(text)
                }

                item { HorizontalDivider() }

                item { Text("Steps", style = MaterialTheme.typography.titleMedium) }
                items(data.steps.sortedBy { it.sortOrder }) { step ->
                    Text("• ${step.instruction}")
                }
            }
        }
    }
}
