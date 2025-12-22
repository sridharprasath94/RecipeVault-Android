package com.flash.recipeVault.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.firebase.FirestoreSyncService
import com.google.firebase.auth.FirebaseAuth
import com.flash.recipeVault.firebase.FirebaseImageStorage

class PeriodicFirebaseBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val user = FirebaseAuth.getInstance().currentUser ?: return Result.success()
        val dbName = "recipe_db_${user.uid}"
        val db = RecipeDatabase.get(applicationContext, dbName)
        val imageStorage = FirebaseImageStorage(applicationContext,
            auth = FirebaseAuth.getInstance()
        )
        val repo = RecipeRepository(
            dao = db.recipeDao(),
            imageStorage = imageStorage
        )
        return try {
            FirestoreSyncService(repo).syncNow()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
