package com.flash.recipeVault.ui.screens.recipeDetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
    val ingredients = remember(data.ingredients) { data.ingredients.sortedBy { it.sortOrder } }
    val steps = remember(data.steps) { data.steps.sortedBy { it.sortOrder } }

    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(data.recipe.title, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(10.dp))

            if (!data.recipe.description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(data.recipe.description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(10.dp))

            data.recipe.imageUrl?.let { url ->
                Spacer(Modifier.height(12.dp))
                RecipeAsyncImage(
                    model = url,
                )
            }
        }

        item {
            SectionCard(title = "Ingredients") {
                if (ingredients.isEmpty()) {
                    Text("No ingredients added.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    ingredients.forEachIndexed { index, ing ->
                        IngredientRowItem(
                            index = index + 1,
                            name = ing.name,
                            quantity = ing.quantity,
                            unit = ing.unit
                        )
                        if (index != ingredients.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }

        item {
            SectionCard(title = "Steps") {
                if (steps.isEmpty()) {
                    Text("No steps added.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    steps.forEachIndexed { index, step ->
                        StepRowItem(
                            index = index + 1,
                            instruction = step.instruction
                        )
                        if (index != steps.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun IngredientRowItem(
    index: Int,
    name: String,
    quantity: String?,
    unit: String?
) {
    val meta = listOfNotNull(quantity?.takeIf { it.isNotBlank() }, unit?.takeIf { it.isNotBlank() })
        .joinToString(" ")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = MaterialTheme.typography.bodyLarge)
            if (meta.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepRowItem(
    index: Int,
    instruction: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = index.toString(),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

