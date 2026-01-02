package com.flash.recipeVault.ui.screens.createRecipe

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.flash.recipeVault.ui.components.FormTopBar
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.RecipeForm
import com.flash.recipeVault.ui.components.rememberAnimatedImeBottomPadding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    vm: CreateRecipeViewModel,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val ui by vm.ui.collectAsState()
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> vm.onPickedImage(uri?.toString()) }
    )
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            vm.onScreenVisible()
        }
    }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is CreateRecipeEvent.Toast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }

                is CreateRecipeEvent.OnFinishedSaving -> {
                    vm.startNavigation()
                    onCreated(event.id)
                }

                CreateRecipeEvent.OnBackClicked -> {
                    vm.startNavigation()
                    onBack()
                }
            }
        }
    }

    CreateRecipeContent(
        ui = ui,
        onBack = vm::requestBack,
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
fun CreateRecipeContent(
    ui: CreateRecipeUiState,
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
    val isInteractionEnabled = !ui.isSaving && !ui.isNavigating
    Scaffold(
        modifier = Modifier.padding(bottom = imePadding),
        topBar = {
            FormTopBar(
                title = "New Recipe",
                actionLabel = "Save",
                isInteractionEnabled = isInteractionEnabled,
                isActionInProgress = ui.isSaving,
                onBack = onBack,
                onPrimaryAction = onSave
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeForm(
                padding = padding,
                isLoading = false,
                title = ui.title,
                suggestions = ui.suggestions,
                onTitleChange = onTitleChange,
                desc = ui.description,
                onDescChange = onDescChange,
                pickedImageUri = ui.pickedImageUri,
                existingImageUrl = null,
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

            if (ui.isSaving) {
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