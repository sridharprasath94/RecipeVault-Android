package com.flash.recipeVault.ui.screens.createRecipe

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.components.AddItemButton
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.IngredientItem
import com.flash.recipeVault.ui.components.RecipeEditFields
import com.flash.recipeVault.ui.components.RecipeImagePicker
import com.flash.recipeVault.ui.components.StepItemRow
import com.flash.recipeVault.ui.model.SuggestionsUi
import com.flash.recipeVault.ui.screens.recipeDetail.SectionCard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rememberAnimatedImeBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    vm: CreateRecipeViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val suggestions by vm.suggestions.collectAsState()
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> vm.onPickedImage(uri?.toString()) }
    )
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val imePadding = rememberAnimatedImeBottomPadding()

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is CreateRecipeEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is CreateRecipeEvent.OnFinishedSaving -> {
                    onCreated(event.id)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            CreateRecipeTopBar(
                title = "New Recipe",
                onBack = onBack,
                isSaving = ui.isSaving,
                onSave = {
                    keyboardController?.hide()
                    vm.save()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CreateRecipeForm(
                padding = padding,
                title = ui.title,
                suggestions = suggestions,
                onTitleChange = vm::updateTitle,
                desc = ui.description,
                onDescChange = vm::updateDescription,
                imageUri = ui.pickedImageUri,
                onPickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = vm::onRemoveImage,
                ingredients = ui.ingredients,
                onIngredientChange = vm::onIngredientChanged,
                onIngredientRemove = vm::onIngredientRemoved,
                onAddIngredient = {
                    vm.onAddIngredient()
                    scope.launch {
                        listState.animateScrollToItem(
                            listState.layoutInfo.totalItemsCount.coerceAtLeast(
                                1
                            ) - 1
                        )
                    }
                },
                steps = ui.steps,
                onStepChange = vm::onStepChanged,
                onStepsRemove = vm::onStepRemoved,
                onAddStep = {
                    vm.onAddStep()
                    scope.launch {
                        listState.animateScrollToItem(
                            listState.layoutInfo.totalItemsCount.coerceAtLeast(
                                1
                            ) - 1
                        )
                    }
                }
            )

            if (ui.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                        // Consume all pointer input so nothing beneath is clickable
                        .pointerInput(Unit) { /* just block */ }
                )

                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeTopBar(
    title: String,
    onBack: () -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
) {
    Box {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            },
            actions = {
                TextButton(onClick = onSave, enabled = !isSaving) {
                    if (isSaving) CircularProgressIndicator(
                        Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    else Text("Save")
                }
            }
        )

        if (isSaving) {
            // 🔒 Touch-blocking overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(Unit) { /* consume all touches */ }
            )
        }
    }
}

@Composable
fun CreateRecipeForm(
    padding: PaddingValues,
    title: String,
    suggestions: SuggestionsUi,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    imageUri: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    ingredients: List<IngredientFormRow>,
    onIngredientChange: (Int, IngredientFormRow) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    steps: List<String>,
    onStepChange: (Int, String) -> Unit,
    onStepsRemove: (Int) -> Unit,
    onAddStep: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RecipeEditFields(
                title = title,
                onTitleChange = onTitleChange,
                desc = desc,
                onDescChange = onDescChange,
            )
        }

        item {
            RecipeImagePicker(
                pickedImageUri = imageUri,
                existingImageUrl = null,
                onPickClick = onPickImage,
                onRemoveClick = onRemoveImage
            )
        }

        item {
            SectionCard(title = "Ingredients") {
                if (ingredients.isEmpty()) {
                    Text("No ingredients added.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    ingredients.forEachIndexed { idx, row ->
                        IngredientItem(
                            index = idx + 1,
                            suggestions = suggestions,
                            row = row,
                            onChange = { onIngredientChange(idx, it) },
                            onRemove = { onIngredientRemove(idx) },
                        )
                        if (idx != ingredients.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }

        item {
            AddItemButton(
                text = "Add ingredient",
                onClick = onAddIngredient
            )
        }

        item {
            SectionCard(title = "Steps") {
                if (steps.isEmpty()) {
                    Text("No steps added.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    steps.forEachIndexed { idx, step ->
                        StepItemRow(
                            s = step,
                            suggestions = suggestions,
                            idx = idx + 1,
                            onChange = { onStepChange(idx, it) },
                            onRemove = { onStepsRemove(idx) })
                        if (idx != steps.lastIndex) {
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        item {
            AddItemButton(
                text = "Add step",
                onClick = onAddStep
            )
        }
    }
}
