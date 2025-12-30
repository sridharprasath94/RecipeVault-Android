package com.flash.recipeVault.ui.screens.editRecipe

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.flash.recipeVault.ui.components.FormTopBar
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.RecipeForm
import com.flash.recipeVault.ui.model.SuggestionsUi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rememberAnimatedImeBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    vm: EditRecipeViewModel,
    onBack: () -> Unit
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

    var isFinishing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is EditRecipeEvent.Toast -> {
                    if (isFinishing) return@collectLatest
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

    EditRecipeContent(
        ui = ui,
        suggestions = suggestions,
        isFinishing = isFinishing,
        onBack = onBack,
        onSave = {
            keyboardController?.hide()
            vm.save()
        },
        onTitleChange = vm::updateTitle,
        onDescChange = vm::updateDescription,
        onPickImage = {
            pickImageLauncher.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        },
        onRemoveImage = vm::onRemoveImage,
        onIngredientChange = vm::onIngredientChanged,
        onIngredientRemove = vm::onIngredientRemoved,
        onIngredientAdd = {
            vm.onAddIngredient()
            scope.launch {
                listState.animateScrollToItem(ui.ingredients.lastIndex)
            }
        },
        onStepChange = vm::onStepChanged,
        onStepRemove = vm::onStepRemoved,
        onStepAdd = {
            vm.onAddStep()
            scope.launch {
                listState.animateScrollToItem(ui.steps.lastIndex)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeContent(
    ui: EditRecipeUiState,
    suggestions: SuggestionsUi,
    isFinishing: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onIngredientChange: (Int, IngredientFormRow) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    onIngredientAdd: () -> Unit,
    onStepChange: (Int, String) -> Unit,
    onStepRemove: (Int) -> Unit,
    onStepAdd: () -> Unit,
) {
    val imePadding = rememberAnimatedImeBottomPadding()
    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            FormTopBar(
                title = "Edit Recipe",
                actionLabel = "Save",
                isInteractionEnabled = !ui.isSaving && !ui.isLoadingData && !isFinishing,
                onBack = onBack,
                onPrimaryAction = onSave
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {

            RecipeForm(
                padding = padding,
                isLoading = ui.isLoadingData,
                title = ui.title,
                suggestions = suggestions,
                onTitleChange = onTitleChange,
                desc = ui.description,
                onDescChange = onDescChange,
                pickedImageUri = ui.pickedImageUri,
                existingImageUrl = ui.existingImageUrl,
                onPickImage = onPickImage,
                onRemoveImage = onRemoveImage,
                ingredients = ui.ingredients,
                onIngredientChange = onIngredientChange,
                onIngredientRemove = onIngredientRemove,
                onIngredientAdd = onIngredientAdd,
                steps = ui.steps,
                onStepChange = onStepChange,
                onStepRemove = onStepRemove,
                onStepAdd = onStepAdd
            )

            if (ui.isSaving || ui.isLoadingData) {
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
