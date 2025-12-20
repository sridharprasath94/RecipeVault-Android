package com.flash.recipeVault.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.flash.recipeVault.data.RecipeDatabase
import com.flash.recipeVault.data.RecipeRepository
import com.flash.recipeVault.ui.theme.RecipeSaverTheme
import org.junit.Rule
import org.junit.Test

class RecipeListScreenTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyState() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java).allowMainThreadQueries().build()
        val repo = RecipeRepository(db.recipeDao())

        rule.setContent {
            RecipeSaverTheme {
                RecipeListScreenForTest(repo = repo)
            }
        }

        rule.onNodeWithText("No recipes yet. Tap + to add one.").assertIsDisplayed()
        db.close()
    }
}
