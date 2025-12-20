package com.flash.recipeVault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeRepositoryInstrumentedTest {

    private lateinit var db: RecipeDatabase
    private lateinit var repo: RecipeRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RecipeRepository(db.recipeDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createRecipe_thenAppearsInList() = runBlocking {
        val id = repo.createRecipe(
            title = "Pasta",
            description = "Quick dinner",
            imageUri = null,
            ingredients = listOf(Triple("Noodles", "200", "g")),
            steps = listOf("Boil water", "Cook noodles")
        )

        val list = repo.observeRecipes().first()
        assertEquals(1, list.size)
        assertEquals(id, list.first().id)
        assertEquals("Pasta", list.first().title)
    }
}
