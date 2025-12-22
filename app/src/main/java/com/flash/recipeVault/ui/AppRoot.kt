package com.flash.recipeVault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.google.firebase.firestore.ListenerRegistration
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.screens.auth.AuthScreen
import com.flash.recipeVault.ui.screens.createRecipe.CreateRecipeScreen
import com.flash.recipeVault.ui.theme.RecipeVaultTheme
import com.flash.recipeVault.ui.screens.auth.AuthState
import com.flash.recipeVault.ui.screens.auth.AuthViewModel
import com.flash.recipeVault.ui.screens.editRecipe.EditRecipeScreen
import com.flash.recipeVault.ui.screens.recipeDetail.RecipeDetailScreen
import com.flash.recipeVault.ui.screens.recipeList.RecipeListScreen

@Composable
fun AppRoot(container: AppContainer) {
    RecipeVaultTheme {
        val nav = rememberNavController()
        val authVm = remember { AuthViewModel() }
        val state by authVm.state.collectAsState()

        val uid = (state as? AuthState.LoggedIn)?.uid

        val realtimeRegState = remember { mutableStateOf<ListenerRegistration?>(null) }

        DisposableEffect(uid) {
            if (uid != null) {
                realtimeRegState.value =
                    container.firestoreSyncServiceForCurrentUser().startRealtime()
            }
            onDispose {
                realtimeRegState.value?.remove()
                realtimeRegState.value = null
            }
        }

        LaunchedEffect(state) {
            if (state is AuthState.LoggedIn) {
                container.firestoreSyncServiceForCurrentUser().syncNow()
            }
        }

        val start = if (state is AuthState.LoggedIn) "list" else "login"

        NavHost(navController = nav, startDestination = start) {
            composable("login") {
                AuthScreen(
                    authVm = authVm,
                    onLoggedIn = { nav.navigate("list") { popUpTo("login") { inclusive = true } } }
                )
            }

            composable("list") {
                LocalContext.current.deleteDatabase( "recipe_db_${(state as? AuthState.LoggedIn)?.uid}")
                RecipeListScreen(
                    container = container,
                    onAdd = { nav.navigate("create") },
                    onOpen = { id -> nav.navigate("detail/$id") },
                    onLogout = {

                        nav.navigate("login") { popUpTo("list") { inclusive = true } }
                    }
                )
            }
            composable("create") {
                CreateRecipeScreen(
                    container = container,
                    onBack = { nav.popBackStack() },
                    onCreated = { id ->
                        nav.popBackStack()
                        nav.navigate("detail/$id")
                    }
                )
            }
            composable("edit/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")!!.toLong()
                EditRecipeScreen(
                    container = container,
                    recipeId = id,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("detail/{id}") { backStack ->
                val id = backStack.arguments?.getString("id")!!.toLong()
                RecipeDetailScreen(
                    container = container,
                    recipeId = id,
                    onBack = { nav.popBackStack() },
                    onEdit = { rid -> nav.navigate("edit/$rid") }
                )
            }
        }
    }
}
