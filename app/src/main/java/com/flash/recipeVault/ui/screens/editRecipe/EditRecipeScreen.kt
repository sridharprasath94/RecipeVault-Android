package com.flash.recipeVault.ui.screens.editRecipe

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun EditRecipeScreen(
    container: AppContainer,
    recipeId: Long,
    onBack: () -> Unit
) {
    val ingredientsSuggestionRepo = container.ingredientSuggestionsRepositoryForCurrentUser()
    val repo = remember { container.recipeRepositoryForCurrentUser() }
    val vm = remember { EditRecipeViewModel(recipeId, repo, ingredientsSuggestionRepo) }
    val data by vm.recipe.collectAsState()
    val suggestions by container.ingredientSuggestionsRepositoryForCurrentUser().observeAllMerged()
        .collectAsState(initial = emptyList())

    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf(IngredientFormRow()) }
    val steps = remember { mutableStateListOf("") }
    var pickedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var alreadyAvailableImageUrl by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            pickedImageUri = uri?.toString()
            alreadyAvailableImageUrl = null
        }
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

    LaunchedEffect(data?.recipe?.id) {
        val r = data ?: return@LaunchedEffect
        title = r.recipe.title
        desc = r.recipe.description ?: ""
        alreadyAvailableImageUrl = r.recipe.imageUrl

        ingredients.clear()
        val ing = r.ingredients.sortedBy { it.sortOrder }
        if (ing.isEmpty()) ingredients.add(0, IngredientFormRow())
        else ing.forEach {
            ingredients.add(
                IngredientFormRow(
                    it.name,
                    it.quantity ?: "",
                    it.unit ?: ""
                )
            )
        }

        steps.clear()
        val st = r.steps.sortedBy { it.sortOrder }
        if (st.isEmpty()) steps.add("")
        else st.forEach { steps.add(it.instruction) }
    }

    Scaffold(
        topBar = {
            EditRecipeTopBar(
                title = "Edit Recipe",
                onBack = onBack,
                isSaving = ui.isSaving,
                onSave = {
                    vm.save(
                        title = title,
                        description = desc,
                        imageUri = pickedImageUri,
                        imageUrl = alreadyAvailableImageUrl,
                        ingredients = ingredients.toList(),
                        steps = steps.toList(),
                        onDone = onBack,
                    )
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            EditRecipeForm(
                padding = padding,
                isLoading = data == null,
                title = title,
                suggestions = suggestions,
                onTitleChange = { title = it },
                desc = desc,
                onDescChange = { desc = it },
                pickedImageUri = pickedImageUri,
                alreadyAvailableImageUrl = alreadyAvailableImageUrl,
                onPickImage = {
                    pickImageLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onRemoveImage = {
                    pickedImageUri = null
                    alreadyAvailableImageUrl = null
                },
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
                },
            )

            // ✅ Overlay that blocks touches + dims UI
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
fun EditRecipeTopBar(
    title: String,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
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
fun EditRecipeForm(
    padding: PaddingValues,
    isLoading: Boolean,
    title: String,
    suggestions: List<String>,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    pickedImageUri: String?,
    alreadyAvailableImageUrl: String?,
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
