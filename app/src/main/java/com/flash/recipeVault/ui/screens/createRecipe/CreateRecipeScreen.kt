package com.flash.recipeVault.ui.screens.createRecipe

import android.util.Log
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.AddItemButton
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.IngredientItem
import com.flash.recipeVault.ui.components.RecipeEditFields
import com.flash.recipeVault.ui.components.RecipeImagePicker
import com.flash.recipeVault.ui.components.StepItemRow
import com.flash.recipeVault.ui.screens.recipeDetail.SectionCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val vm = remember { CreateRecipeViewModel(container.recipeRepositoryForCurrentUser()) }

    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf(IngredientFormRow()) }
    val steps = remember { mutableStateListOf("") }

    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri?.toString() }
    )
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val ui by vm.ui.collectAsState()
    val errorMessage = (ui.error)
    val context = LocalContext.current


    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            Log.d("AuthScreen", "Authentication error: $errorMessage")
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            vm.clearError()
        }
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            CreateRecipeTopBar(
                title = "New Recipe",
                onBack = onBack,
                isSaving = ui.isSaving,
                onSave = {
                    keyboardController?.hide()

                    vm.save(
                        title = title,
                        description = desc,
                        imageUri = imageUri,
                        imageUrl = null,
                        ingredients = ingredients.toList(),
                        steps = steps.toList(),
                        onDone = onCreated,
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            CreateRecipeForm(
                padding = padding,
                title = title,
                onTitleChange = { title = it },
                desc = desc,
                onDescChange = { desc = it },
                imageUri = imageUri,
                onPickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { imageUri = null },
                ingredients = ingredients,
                onIngredientChange = { idx, row -> ingredients[idx] = row },
                onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
                onAddIngredient = {
                    ingredients.add(IngredientFormRow())
                    scope.launch { listState.animateScrollToItem(ingredients.lastIndex) }
                },
                steps = steps,
                onStepChange = { idx, value -> steps[idx] = value },
                onStepsRemove = { idx -> if (steps.size > 1) steps.removeAt(idx) },
                onAddStep = {
                    steps.add("")
                    scope.launch { listState.animateScrollToItem(steps.lastIndex) }
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
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close") }
        },
        actions = {
            TextButton(onClick = onSave, enabled = !isSaving) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    )
}

@Composable
fun CreateRecipeForm(
    padding: PaddingValues,
    title: String,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    imageUri: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    ingredients: SnapshotStateList<IngredientFormRow>,
    onIngredientChange: (Int, IngredientFormRow) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    steps: SnapshotStateList<String>,
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

        item { Text("Ingredients", style = MaterialTheme.typography.titleMedium) }

        item {
            SectionCard(title = "Ingredients") {
                if (ingredients.isEmpty()) {
                    Text("No ingredients added.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    ingredients.forEachIndexed { idx, row ->
                        IngredientItem(
                            index = idx + 1,
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

