package com.flash.recipeVault.di

import android.content.Context
import com.flash.recipeVault.data.SuggestionsRepository
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.data.SuggestionType
import com.flash.recipeVault.data.defaults.DefaultSuggestionsProvider
import com.flash.recipeVault.firebase.FirebaseBackupService
import com.flash.recipeVault.firebase.FirebaseImageStorage
import com.flash.recipeVault.firebase.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth

/**
 * Simple DI container / Service Locator for the app.
 *
 * Why this exists:
 * - Centralizes creation of app-wide dependencies (Auth, DB, Repository, Sync services)
 * - Ensures a consistent singleton-ish lifecycle (avoid recreating DB/repo every call)
 * - Supports per-user data isolation by using a user-specific DB name
 */

class AppContainer(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    // Use applicationContext to avoid leaking an Activity
    private val appContext: Context = context.applicationContext

    /**
     * Image storage is not user-specific by itself, but it needs Auth to build per-user paths.
     * Keep a single instance.
     */
    private val imageStorage: FirebaseImageStorage by lazy {
        FirebaseImageStorage(appContext, auth = auth)
    }

    // Cache per-user instances to avoid recreating them on every call.
    private var cachedUid: String? = null
    private var cachedDb: RecipeDatabase? = null
    private var cachedRecipeRepo: RecipeRepository? = null
    private var cachedSuggestionsRepo: SuggestionsRepository? = null
    private var cachedSync: FirestoreSyncService? = null
    private var cachedBackup: FirebaseBackupService? = null

    /**
     * Signs out the current user and clears all user-scoped caches.
     */
    fun signOut() {
        clearUserScopedCaches()
        auth.signOut()
    }

    /**
     * Clears all instances that are scoped to the currently logged in user.
     * Call this on logout or whenever user changes.
     */
    private fun clearUserScopedCaches() {
        cachedUid = null
        cachedDb = null
        cachedRecipeRepo = null
        cachedSuggestionsRepo = null
        cachedSync = null
        cachedBackup = null
    }

    /**
     * Ensures caches match the current user. If user changed, rebuild user-scoped objects.
     */
    private fun ensureUserCache(): String {
        val uid = auth.currentUser?.uid ?: error("User not logged in")
        if (cachedUid != uid) {
            clearUserScopedCaches()
            cachedUid = uid
        }
        return uid
    }

    /**
     * Repository scoped to the logged-in user.
     *
     * DB name is user-specific => local data is isolated per account.
     */
    fun recipeRepositoryForCurrentUser(): RecipeRepository {
        val uid = ensureUserCache()

        val db = cachedDb ?: RecipeDatabase.get(appContext, "recipe_db_$uid").also {
            cachedDb = it
        }

        return cachedRecipeRepo ?: RecipeRepository(db.recipeDao(), imageStorage).also {
            cachedRecipeRepo = it
        }
    }

    fun suggestionsRepositoryForCurrentUser(): SuggestionsRepository {
        val uid = ensureUserCache()

        val db = cachedDb ?: RecipeDatabase.get(appContext, "recipe_db_$uid").also {
            cachedDb = it
        }

        return cachedSuggestionsRepo ?: SuggestionsRepository(db.suggestionDao()).also {
            cachedSuggestionsRepo = it
        }
    }

    /**
     * Firestore two-way sync service.
     * Used by:
     * - SyncNow button (one-shot sync)
     * - startRealTime (snapshot listener while app is open)
     * - periodic workers (if you schedule them)
     */
    fun firestoreSyncServiceForCurrentUser(): FirestoreSyncService {
        ensureUserCache()
        return cachedSync ?: FirestoreSyncService(
            recipeRepositoryForCurrentUser(),
            auth = auth
        ).also {
            cachedSync = it
        }
    }

    /**
     * Used for manual backup/export flows (JSON export, Drive share, etc.).
     */
    fun firebaseBackupServiceForCurrentUser(): FirebaseBackupService {
        ensureUserCache()
        return cachedBackup ?: FirebaseBackupService(
            recipeRepositoryForCurrentUser(),
            auth = auth
        ).also {
            cachedBackup = it
        }
    }

    /**
     * One-time seeding of default suggestions from res/raw text files.
     * Safe to call on every app start; it will no-op if data already exists.
     *
     * Call this from Application.onCreate (inside a Dispatchers.IO coroutine).
     */
    suspend fun seedDefaultSuggestionsIfEmpty() {
        val repo = suggestionsRepositoryForCurrentUser()

        repo.seedDefaultsIfEmpty(
            type = SuggestionType.INGREDIENT,
            defaults = DefaultSuggestionsProvider.ingredients(appContext)
        )
        repo.seedDefaultsIfEmpty(
            type = SuggestionType.UNIT,
            defaults = DefaultSuggestionsProvider.units(appContext)
        )
        repo.seedDefaultsIfEmpty(
            type = SuggestionType.STEP,
            defaults = DefaultSuggestionsProvider.steps(appContext)
        )
    }
}
