package com.flash.recipeVault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.flash.recipeVault.worker.PeriodicFirebaseBackupWorker
import com.google.firebase.FirebaseApp
import com.flash.recipeVault.di.AppContainer
import com.flash.recipeVault.ui.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        val container = AppContainer(this)
        val request =
            PeriodicWorkRequestBuilder<PeriodicFirebaseBackupWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "periodic_firebase_backup",
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )

        setContent { AppRoot(container) }
    }
}
