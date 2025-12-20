package com.flash.recipeVault.di

import android.content.Context
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.firebase.FirebaseBackupService
import com.flash.recipeVault.firebase.FirebaseImageStorage
import com.flash.recipeVault.firebase.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth

class AppContainer(private val context: Context) {
    private val imageStorage by lazy {
        FirebaseImageStorage(
            context,
            auth = auth
        )
    }


    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid

    fun signOut() = auth.signOut()

    fun recipeRepositoryForCurrentUser(): RecipeRepository {
        val uid = auth.currentUser?.uid ?: error("User not logged in")
        val db = RecipeDatabase.get(context, "recipe_db_$uid")
        return RecipeRepository(db.recipeDao(), imageStorage)
    }

    fun firebaseBackupServiceForCurrentUser(): FirebaseBackupService {
        return FirebaseBackupService(recipeRepositoryForCurrentUser(), auth = auth)
    }

    fun firestoreSyncServiceForCurrentUser(): FirestoreSyncService {
        return FirestoreSyncService(
            context,
            recipeRepositoryForCurrentUser(),
            auth = FirebaseAuth.getInstance()
        )
    }
}
