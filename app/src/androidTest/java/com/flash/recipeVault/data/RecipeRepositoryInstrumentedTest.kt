package com.flash.recipeVault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.flash.recipeVault.firebase.FirebaseImageStorage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeRepositoryInstrumentedTest {

    private lateinit var db: RecipeDatabase
    private lateinit var repo: RecipeRepository

    /**
     * Fake image storage for tests so we never call real Firebase.
     * - upload returns a deterministic URL
     * - delete is a no-op
     */
    private class FakeImageStorage(context: Context) : FirebaseImageStorage(
        context = context,
        auth = null
    ) {
        override suspend fun uploadRecipeImage(recipeId: Long, localUri: String): String {
            return "https://example.test/images/$recipeId.jpg"
        }

        override suspend fun deleteByUrl(downloadUrl: String) {
            // no-op
        }
    }

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = RecipeRepository(
            dao = db.recipeDao(),
            imageStorage = FakeImageStorage(context)
        )
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
            imageUri = "content://dummy",
            imageUrl = null,
            ingredients = listOf(Triple("Noodles", "200", "g")),
            steps = listOf("Boil water", "Cook noodles")
        )

        val list = repo.observeRecipes().first()
        assertEquals(1, list.size)
        assertEquals(id, list.first().id)
        assertEquals("Pasta", list.first().title)

        // Since imageUri was provided and imageUrl was null, repository should have uploaded and saved a URL.
        assertTrue(list.first().imageUrl?.startsWith("https://example.test/") == true)
    }

    @Test
    fun deleteRecipe_marksDeleted_andIsHiddenFromObserveRecipes() = runBlocking {
        val id = repo.createRecipe(
            title = "To Delete",
            description = null,
            imageUri = null,
            imageUrl = null,
            ingredients = emptyList(),
            steps = emptyList()
        )

        repo.deleteRecipe(id)

        val visible = repo.observeRecipes().first()
        assertTrue(visible.none { it.id == id })

        val all = repo.getAllLocalRecipesIncludingDeleted()
        val deleted = all.first { it.id == id }
        assertTrue(deleted.isDeleted)
    }
}
