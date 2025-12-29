package com.flash.recipeVault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.flash.recipeVault.worker.PeriodicFirebaseBackupWorker
import com.google.firebase.FirebaseApp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.AppRoot
import com.flash.recipeVault.ui.theme.RecipeVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
//        val request =
//            PeriodicWorkRequestBuilder<PeriodicFirebaseBackupWorker>(6, TimeUnit.HOURS).build()
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "periodic_firebase_backup",
//            ExistingPeriodicWorkPolicy.UPDATE,
//            request
//        )

        setContent {
            RecipeVaultTheme {
                val container = remember {
                    AppContainer(applicationContext)

                }
                AppRoot(container)
            }
        }
    }
}