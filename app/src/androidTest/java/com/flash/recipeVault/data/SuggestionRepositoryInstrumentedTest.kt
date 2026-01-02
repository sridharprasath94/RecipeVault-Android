package com.flash.recipeVault.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SuggestionRepositoryInstrumentedTest {

    private lateinit var db: RecipeDatabase
    private lateinit var repo: SuggestionsRepository

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, RecipeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = SuggestionsRepository(db.suggestionDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun add_thenObserveMerged_containsValue() = runBlocking {
        repo.add(type = SuggestionType.INGREDIENT, value = "Onion")
        repo.add(type = SuggestionType.INGREDIENT, value = "Tomato")

        val merged = repo.observeAllMerged(type = SuggestionType.INGREDIENT).first()
        assertTrue(merged.contains("Onion"))
        assertTrue(merged.contains("Tomato"))
    }

    @Test
    fun searchPrefix_isCaseInsensitive_andReturnsLimitedResults() = runBlocking {
        repo.add(type = SuggestionType.INGREDIENT, value = "Onion")
        repo.add(type = SuggestionType.INGREDIENT, value = "Onion Powder")
        repo.add(type = SuggestionType.INGREDIENT, value = "Tomato")

        val results = repo.searchPrefix(type = SuggestionType.INGREDIENT, query = "on", limit = 1)
        assertEquals(1, results.size)
        assertTrue(results.first().startsWith("Onion"))

        val results2 =
            repo.searchPrefix(type = SuggestionType.INGREDIENT, query = "ON", limit = 10)
        assertTrue(results2.isNotEmpty())
        assertTrue(results2.all { it.lowercase().startsWith("on") })
    }

    @Test
    fun seedDefaultsIfEmpty_runsOnce() = runBlocking {
        val defaults = listOf("Salt", "Pepper")

        repo.seedDefaultsIfEmpty(type = SuggestionType.UNIT, defaults = defaults)
        val first = repo.observeAllMerged(type = SuggestionType.UNIT).first()
        assertTrue(first.contains("Salt"))

        // second call should not duplicate
        repo.seedDefaultsIfEmpty(type = SuggestionType.UNIT, defaults = defaults)
        val second = repo.observeAllMerged(type = SuggestionType.UNIT).first()
        assertEquals(first.size, second.size)
    }
}
