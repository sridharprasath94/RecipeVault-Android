package com.flash.recipeVault.ui.createRecipe

import com.flash.recipeVault.ui.components.IngredientFormRow
import com.flash.recipeVault.ui.components.IngredientRow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.createRecipe.CreateRecipeViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.flash.recipeVault.util.RecipeImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRecipeScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val repo = remember { container.recipeRepositoryForCurrentUser() }
    val vm = remember { CreateRecipeViewModel(repo) }

    var title by rememberSaveable { mutableStateOf("") }
    var desc by rememberSaveable { mutableStateOf("") }

    val ingredients = remember { mutableStateListOf(IngredientFormRow()) }
    val steps = remember { mutableStateListOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var imageUri by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri?.toString() }
    )

    val keyboardController = LocalSoftwareKeyboardController.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
                },
                actions = {
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            error = null

                            vm.save(
                                title = title.trim(),
                                description = desc,
                                imageUri = imageUri,
                                imageUrl = null,
                                ingredients = ingredients.toList(),
                                steps = steps.toList(),
                                onDone = onCreated,
                                onError = { error = it }
                            )
                        }
                    ) { Text("Save") }
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = {
                        pickImageLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }) { Text(if (imageUri == null) "Pick image (optional)" else "Change image") }

                    if (imageUri != null) {
                        OutlinedButton(onClick = { imageUri = null }) { Text("Remove") }
                    }
                }

                if (imageUri != null) {
                    Spacer(Modifier.height(8.dp))
                    RecipeImage(imageUri)
                }
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
                OutlinedButton(onClick = { ingredients.add(0, IngredientFormRow()) }) {
                    Icon(
                        Icons.Default.Add,
                        null
                    ); Spacer(Modifier.width(8.dp)); Text("Add ingredient")
                }
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

            item {
                OutlinedButton(onClick = { steps.add("") }) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Add step")
                }
            }

            item {
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

