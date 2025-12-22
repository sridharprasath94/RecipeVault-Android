package com.flash.recipeVault.di

import android.content.Context
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
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
    private var cachedRepo: RecipeRepository? = null
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
        cachedRepo = null
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

        return cachedRepo ?: RecipeRepository(db.recipeDao(), imageStorage).also {
            cachedRepo = it
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
}
