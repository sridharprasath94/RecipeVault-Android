package com.flash.recipeVault.ui.recipeDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.data.RecipeWithDetails
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.util.RecipeAsyncImage

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
            RecipeDetailTopBar(
                recipeId = recipeId,
                onBack = onBack,
                onEdit = onEdit,
                onDelete = {
                    vm.delete()
                    onBack()
                }
            )
        }
    ) { padding ->
        RecipeDetailContent(
            padding = padding,
            data = recipeWithDetails
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun RecipeDetailTopBar(
    recipeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: () -> Unit,
) {
    TopAppBar(
        title = { Text("Recipe") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(onClick = { onEdit(recipeId) }) { Text("Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    )
}

@Composable
fun RecipeDetailContent(
    padding: PaddingValues,
    data: RecipeWithDetails?,
) {
    if (data == null) {
        RecipeDetailLoading(padding = padding)
    } else {
        RecipeDetailBody(padding = padding, data = data)
    }
}

@Composable
private fun RecipeDetailLoading(padding: PaddingValues) {
    Box(
        Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading…")
    }
}

@Composable
fun RecipeDetailBody(
    padding: PaddingValues,
    data: RecipeWithDetails,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                data.recipe.title,
                style = MaterialTheme.typography.headlineSmall
            )

            if (!data.recipe.description.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    data.recipe.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            data.recipe.imageUrl?.let { url ->
                RecipeAsyncImage(url)
            }
        }

        item { HorizontalDivider() }

        item {
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
        }

        items(data.ingredients.sortedBy { it.sortOrder }) { ing ->
            val suffix = listOfNotNull(ing.quantity, ing.unit).joinToString(" ")
            Text(
                if (suffix.isNotBlank())
                    "• ${ing.name} $suffix"
                else
                    "• ${ing.name}"
            )
        }

        item { HorizontalDivider() }

        item {
            Text("Steps", style = MaterialTheme.typography.titleMedium)
        }

        items(data.steps.sortedBy { it.sortOrder }) { step ->
            Text("• ${step.instruction}")
        }
    }
}

