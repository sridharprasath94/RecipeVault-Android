package com.flash.recipeVault.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.firebase.FirebaseImageStorage
import com.flash.recipeVault.ui.theme.RecipeVaultTheme
import org.junit.Rule
import org.junit.Test

class FakeFirebaseImageStorage : FirebaseImageStorage(
    context = ApplicationProvider.getApplicationContext(),
    auth = null
) {
    // Override methods if needed for testing
}

class RecipeListScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyState() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries().build()
        val repo = RecipeRepository(db.recipeDao(), imageStorage = FakeFirebaseImageStorage())

        rule.setContent {
            RecipeVaultTheme {
                RecipeListScreenForTest(repo = repo)
            }
        }

        rule.onNodeWithText("No recipes yet. Tap + to add one.").assertIsDisplayed()
        db.close()
    }
}
