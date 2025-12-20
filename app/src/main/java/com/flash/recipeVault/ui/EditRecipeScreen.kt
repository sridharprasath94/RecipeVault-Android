package com.flash.recipeVault.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.IngredientRow
import com.flash.recipeVault.vm.EditRecipeViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.flash.recipeVault.util.ImageBase64Util
import com.flash.recipeVault.util.rememberBase64ImageBitmap
import androidx.compose.runtime.derivedStateOf

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
    var alreadyAvailableBase64Image by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            pickedImageUri = uri?.toString()
            alreadyAvailableBase64Image = null
        }
    )
    val context = LocalContext.current
    LaunchedEffect(data?.recipe?.id) {
        val r = data ?: return@LaunchedEffect
        title = r.recipe.title
        desc = r.recipe.description ?: ""
        alreadyAvailableBase64Image = r.recipe.imageUrl

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
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    TextButton(onClick = {
                        error = null
                        val cleanTitle = title.trim()
                        if (cleanTitle.isEmpty()) {
                            error = "Title is required"
                            return@TextButton
                        }

                        val ingredientTriples = ingredients
                            .map {
                                Triple(
                                    it.name.trim(),
                                    it.qty.trim().ifEmpty { null },
                                    it.unit.trim().ifEmpty { null })
                            }
                            .filter { it.first.isNotEmpty() }

                        val cleanSteps = steps.map { it.trim() }.filter { it.isNotEmpty() }

                        vm.save(
                            title = cleanTitle,
                            description = desc.trim().ifEmpty { null },
                            imageUri = pickedImageUri,
                            imageUrl = ImageBase64Util.tryReadAsBase64(context, pickedImageUri),
                            ingredients = ingredientTriples,
                            steps = cleanSteps,
                            onDone = onBack,
                            onError = { error = it }
                        )
                    }) { Text("Save") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { if (data == null) Text("Loading…") }

            item {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                RecipeImageSection(
                    pickedImageUri = pickedImageUri,
                    savedBase64 = alreadyAvailableBase64Image,
                    onPickClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveClick = {
                        pickedImageUri = null
                        alreadyAvailableBase64Image = null
                    }
                )
            }

            item { Text("Ingredients", style = MaterialTheme.typography.titleMedium) }

            itemsIndexed(ingredients) { idx, row ->
                IngredientRow(
                    row = row,
                    onChange = {
                        ingredients[idx] = it
                    },
                    onRemove = { if (ingredients.size > 1) ingredients.removeAt(idx) }
                )
            }

            item {
                OutlinedButton(onClick = {
                    ingredients.add(
                        0,
                        IngredientFormRow()
                    )
                }) { Text("Add ingredient") }
            }

            item { Text("Steps", style = MaterialTheme.typography.titleMedium) }

            itemsIndexed(steps) { idx, s ->
                OutlinedTextField(
                    value = s,
                    onValueChange = { steps[idx] = it },
                    label = { Text("Step ${idx + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { OutlinedButton(onClick = { steps.add("") }) { Text("Add step") } }

            item { if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun RecipeImageSection(
    pickedImageUri: String?,
    savedBase64: String?,
    onPickClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    // Prefer the freshly picked local image over the saved Base64.
    val hasAnyImage by remember(pickedImageUri, savedBase64) {
        derivedStateOf { !pickedImageUri.isNullOrBlank() || !savedBase64.isNullOrBlank() }
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
            Image(
                painter = rememberAsyncImagePainter(pickedImageUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
        }

        !savedBase64.isNullOrBlank() -> {
            val bmp = rememberBase64ImageBitmap(savedBase64)
            if (bmp != null) {
                Spacer(Modifier.height(8.dp))
                Image(
                    bitmap = bmp,
                    contentDescription = "Recipe image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}
