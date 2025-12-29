package com.flash.recipeVault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.screens.auth.AuthScreen
import com.flash.recipeVault.ui.screens.auth.AuthState
import com.flash.recipeVault.ui.screens.auth.AuthViewModel
import com.flash.recipeVault.ui.screens.createRecipe.CreateRecipeScreen
import com.flash.recipeVault.ui.screens.editRecipe.EditRecipeScreen
import com.flash.recipeVault.ui.screens.recipeDetail.RecipeDetailScreen
import com.flash.recipeVault.ui.screens.recipeList.RecipeListScreen
import com.flash.recipeVault.ui.screens.recipeList.RecipeListViewModel
import com.flash.recipeVault.ui.theme.RecipeVaultTheme
import com.google.firebase.firestore.ListenerRegistration

object Routes {
    const val AUTH = "auth"
    const val LIST = "list"
    const val CREATE = "create"
    const val DETAIL = "detail"
    const val EDIT = "edit"
}

@Composable
fun AppRoot(container: AppContainer) {
    RecipeVaultTheme {
        val nav = rememberNavController()
        val authVm = remember { AuthViewModel() }
        val authState by authVm.state.collectAsState()


        LaunchedEffect(Unit) {
            runCatching { container.seedDefaultSuggestionsIfEmpty() }
        }

        // Start/stop realtime sync while logged in
        DisposableEffect(authState) {
            val sync = runCatching { container.firestoreSyncServiceForCurrentUser() }.getOrNull()
            if (authState is AuthState.LoggedIn && sync != null) {
                sync.startRealtime()
                onDispose { sync.stopRealTime() }
            } else {
                onDispose { }
            }
        }

        NavHost(
            navController = nav,
            startDestination = if (authState is AuthState.LoggedIn) Routes.LIST else Routes.AUTH
        ) {
            composable(Routes.AUTH) {
                AuthScreen(
                    authVm = authVm,
                    onLoggedIn = {
                        nav.navigate(Routes.LIST) {
                            popUpTo(Routes.AUTH) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(Routes.LIST) {
//                LocalContext.current.deleteDatabase( "recipe_db_${(state as? AuthState.LoggedIn)?.uid}")
                val vm = remember { RecipeListViewModel(container) }
                RecipeListScreen(
                    vm = vm,
                    onAdd = { nav.navigate(Routes.CREATE) },
                    onEdit = { id -> nav.navigate("${Routes.EDIT}/$id") },
                    onOpen = { id -> nav.navigate("${Routes.DETAIL}/$id") },
                    onLoggedOut = {
                        nav.navigate(Routes.AUTH) { popUpTo(Routes.LIST) { inclusive = true } }
                    }
                )
            }
            composable(Routes.CREATE) {
                CreateRecipeScreen(
                    container = container,
                    onBack = { nav.popBackStack() },
                    onCreated = { id ->
                        nav.popBackStack()
                        nav.navigate("${Routes.DETAIL}/$id")
                    }
                )
            }
            composable(
                route = "${Routes.DETAIL}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: return@composable
                RecipeDetailScreen(
                    container = container,
                    recipeId = id,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate("${Routes.EDIT}/$id") }
                )
            }
            composable(
                route = "${Routes.EDIT}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("id") ?: return@composable
                EditRecipeScreen(
                    container = container,
                    recipeId = id,
                    onBack = { nav.popBackStack() }
                )
            }

        }
    }
}
