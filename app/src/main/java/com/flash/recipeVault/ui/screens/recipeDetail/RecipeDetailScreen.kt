package com.flash.recipeVault.ui.screens.recipeDetail

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.recipeVault.ui.components.ConfirmationDialog
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.util.RecipeAsyncImage
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    vm: RecipeDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val ui by vm.ui.collectAsState()
    var isFinishing by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            isFinishing = false
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is RecipeDetailEvent.Deleted -> {
                    if (isFinishing) return@collectLatest
                    isFinishing = true
                    onBack()
                }

                is RecipeDetailEvent.Toast -> {
                    if (isFinishing) return@collectLatest
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is RecipeDetailEvent.OnEditClicked -> {
                    if (isFinishing) return@collectLatest
                    isFinishing = true
                    onEdit(event.recipeId)
                }

                RecipeDetailEvent.OnBackClicked -> {
                    if (isFinishing) return@collectLatest
                    isFinishing = true
                    onBack()
                }
            }
        }
    }

    ConfirmationDialog(
        show = ui.showDeleteDialog,
        title = "Delete recipe?",
        message = "This action cannot be undone.",
        confirmButtonText = "Delete",
        onConfirm = vm::confirmDelete,
        onDismiss = vm::dismissDelete,
    )

    RecipeDetailContent(
        ui = ui,
        isFinishing = isFinishing,
        onBack = vm::requestBack,
        onEdit = vm::requestEdit,
        onDelete = vm::requestDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailContent(
    ui: RecipeDetailUiState,
    isFinishing: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val isInteractionEnabled = !ui.isLoadingData && !isFinishing
    Scaffold(
        topBar = {
            Box {
                TopAppBar(
                    title = { Text("Recipe") },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = isInteractionEnabled) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onEdit, enabled = isInteractionEnabled) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = onDelete, enabled = isInteractionEnabled) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )

                if (!isInteractionEnabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .pointerInput(Unit) { /* consume all touches */ }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeDetailBody(
                padding = padding,
                title = ui.title,
                description = ui.description,
                imageUrl = ui.existingImageUrl,
                ingredients = ui.ingredients,
                steps = ui.steps
            )

            if (!isInteractionEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)
                        )
                )
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun RecipeDetailBody(
    padding: PaddingValues,
    title: String,
    description: String?,
    imageUrl: String?,
    ingredients: List<IngredientFormRow>,
    steps: List<String>,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(title, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(10.dp))

            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(10.dp))

            imageUrl?.let { url ->
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
                        IngredientItemText(
                            index = index + 1,
                            name = ing.name,
                            quantity = ing.qty,
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
                        StepItemText(
                            index = index + 1,
                            instruction = step
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
private fun IngredientItemText(
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
private fun StepItemText(
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

