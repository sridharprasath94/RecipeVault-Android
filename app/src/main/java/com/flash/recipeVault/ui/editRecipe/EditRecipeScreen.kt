package com.flash.recipeVault.ui.editRecipe

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.IngredientRow
import com.flash.recipeVault.util.RecipeAsyncImage
import com.flash.recipeVault.util.RecipeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    container: AppContainer,
    recipeId: Long,
    onBack: () -> Unit
) {
    val repo = remember { container.recipeRepositoryForCurrentUser() }
    val vm = remember { EditRecipeViewModel(recipeId, repo) }
    val data by vm.recipe.collectAsState()

    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf(IngredientFormRow()) }
    val steps = remember { mutableStateListOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pickedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
    var alreadyAvailableImageUrl by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            pickedImageUri = uri?.toString()
            alreadyAvailableImageUrl = null
        }
    )

    val ui by vm.ui.collectAsState()

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
                    error = null
                    val cleanTitle = title.trim()
                    if (cleanTitle.isEmpty()) {
                        error = "Title is required"
                        return@EditRecipeTopBar
                    }

                    val ingredientTriples = ingredients
                        .map {
                            Triple(
                                it.name.trim(),
                                it.qty.trim().ifEmpty { null },
                                it.unit.trim().ifEmpty { null }
                            )
                        }
                        .filter { it.first.isNotEmpty() }

                    val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }

                    vm.save(
                        title = cleanTitle,
                        description = desc.trim().ifEmpty { null },
                        imageUri = pickedImageUri,
                        imageUrl = null,
                        ingredients = ingredientTriples,
                        steps = cleanSteps,
                        onDone = onBack,
                        onError = { error = it }
                    )
                }
            )
        }
    ) { padding ->
        if (ui.isSaving) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        EditRecipeForm(
            padding = padding,
            isLoading = data == null,
            title = title,
            onTitleChange = { title = it },
            desc = desc,
            onDescChange = { desc = it },
            pickedImageUri = pickedImageUri,
            alreadyAvailableImageUrl = alreadyAvailableImageUrl,
            onPickImage = {
                pickImageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemoveImage = {
                pickedImageUri = null
                alreadyAvailableImageUrl = null
            },
            ingredients = ingredients,
            onIngredientChange = { idx, row -> ingredients[idx] = row },
            onIngredientRemove = { idx -> if (ingredients.size > 1) ingredients.removeAt(idx) },
            onAddIngredient = { ingredients.add(0, IngredientFormRow()) },
            steps = steps,
            onStepChange = { idx, value -> steps[idx] = value },
            onAddStep = { steps.add("") },
            error = error,
        )
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
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
    pickedImageUri: String?,
    alreadyAvailableImageUrl: String?,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    ingredients: androidx.compose.runtime.snapshots.SnapshotStateList<IngredientFormRow>,
    onIngredientChange: (Int, IngredientFormRow) -> Unit,
    onIngredientRemove: (Int) -> Unit,
    onAddIngredient: () -> Unit,
    steps: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    onStepChange: (Int, String) -> Unit,
    onAddStep: () -> Unit,
    error: String?,
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
            RecipeImageSection(
                pickedImageUri = pickedImageUri,
                imageUrl = alreadyAvailableImageUrl,
                onPickClick = onPickImage,
                onRemoveClick = onRemoveImage,
            )
        }

        item { Text("Ingredients", style = MaterialTheme.typography.titleMedium) }

        itemsIndexed(ingredients) { idx, row ->
            IngredientRow(
                row = row,
                onChange = { onIngredientChange(idx, it) },
                onRemove = { onIngredientRemove(idx) },
            )
        }

        item {
            OutlinedButton(onClick = onAddIngredient) { Text("Add ingredient") }
        }

        item { Text("Steps", style = MaterialTheme.typography.titleMedium) }

        itemsIndexed(steps) { idx, s ->
            OutlinedTextField(
                value = s,
                onValueChange = { onStepChange(idx, it) },
                label = { Text("Step ${idx + 1}") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { OutlinedButton(onClick = onAddStep) { Text("Add step") } }

        item {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun RecipeEditFields(
    title: String,
    onTitleChange: (String) -> Unit,
    desc: String,
    onDescChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = desc,
        onValueChange = onDescChange,
        label = { Text("Description (optional)") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun RecipeImageSection(
    pickedImageUri: String?,
    imageUrl: String?,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val hasAnyImage by remember(pickedImageUri, imageUrl) {
        derivedStateOf { !pickedImageUri.isNullOrBlank() || !imageUrl.isNullOrBlank() }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onPickClick) {
            Text(
                if (!hasAnyImage) "Pick image (optional)"
                else "Change image"
            )
        }

        if (hasAnyImage) {
            OutlinedButton(onClick = onRemoveClick) { Text("Remove") }
        }
    }

    when {
        !pickedImageUri.isNullOrBlank() -> {
            Spacer(Modifier.height(8.dp))
            RecipeImage(pickedImageUri)
        }

        !imageUrl.isNullOrBlank() -> {
            Spacer(Modifier.height(8.dp))
            RecipeAsyncImage(imageUrl)
        }
    }
}
