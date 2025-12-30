package com.flash.recipeVault.ui.screens.editRecipe

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.ui.components.AddItemButton
import com.flash.recipeVault.ui.components.FormTopBar
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
fun EditRecipeScreen(
    vm: EditRecipeViewModel,
    onBack: () -> Unit
) {
    val data by vm.recipe.collectAsState()
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
    var isFinishing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is EditRecipeEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                EditRecipeEvent.OnFinishedSaving -> {
                    if (isFinishing) return@collectLatest
                    isFinishing = true
                    onBack()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            FormTopBar(
                title = "Edit Recipe",
                actionLabel = "Save",
                onBack = onBack,
                isInteractionEnabled = !ui.isSaving && !ui.isLoadingData && !isFinishing,
                onPrimaryAction = {
                    keyboardController?.hide()
                    vm.save()
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            EditRecipeForm(
                padding = padding,
                isLoading = data == null,
                title = ui.title,
                suggestions = suggestions,
                onTitleChange = vm::updateTitle,
                desc = ui.description,
                onDescChange = vm::updateDescription,
                pickedImageUri = ui.pickedImageUri,
                alreadyAvailableImageUrl = ui.existingImageUrl,
                onPickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemoveImage = vm::onRemoveImage,
                ingredients = ui.ingredients,
                onIngredientChange = vm::onIngredientChanged,
                onIngredientRemove = vm::onIngredientRemoved,
                onAddIngredient = {
                    vm.onAddIngredient()
                    scope.launch {
                        listState.animateScrollToItem(ui.ingredients.lastIndex)
                    }
                },
                steps = ui.steps,
                onStepChange = vm::onStepChanged,
                onStepsRemove = vm::onStepRemoved,
                onAddStep = {
                    vm.onAddStep()
                    scope.launch {
                        listState.animateScrollToItem(ui.steps.lastIndex)
                    }
                },
            )

            // ✅ Overlay that blocks touches + dims UI
            if (ui.isSaving || ui.isLoadingData) {
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

@Composable
fun EditRecipeForm(
    padding: PaddingValues,
    isLoading: Boolean,
    title: String,
    suggestions: SuggestionsUi,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    pickedImageUri: String?,
    alreadyAvailableImageUrl: String?,
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
            if (isLoading) {
                Text("Loading…")
            }
        }

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
                pickedImageUri = pickedImageUri,
                existingImageUrl = alreadyAvailableImageUrl,
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
                text = "Add Step",
                onClick = onAddStep
            )
        }
    }
}
