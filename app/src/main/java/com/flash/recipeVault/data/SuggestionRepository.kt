package com.flash.recipeVault.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SuggestionsRepository(
    private val dao: SuggestionDao
) {

    fun observeAllMerged(type: SuggestionType): Flow<List<String>> {
        return dao.observeAll(type).map { dbList ->
            (dbList.map { it.value })
                .asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .sorted()
                .toList()
        }
    }

    /** Prefix search from Room only (fast + offline). */
    suspend fun searchPrefix(type: SuggestionType, query: String, limit: Int = 20): List<String> {
        val p = query.trim().lowercase()
        if (p.isBlank()) return emptyList()

        return dao.searchPrefix(type = type, prefix = p, limit = limit)
            .map { it.value }
    }

    /** Persist a user-added suggestion (IGNORE duplicates). */
    suspend fun add(type: SuggestionType, value: String) {
        val clean = value.trim()
        if (clean.isBlank()) return

        val lower = clean.lowercase()
        dao.insert(
            SuggestionEntity(
                key = "${type.name}:$lower",
                type = type,
                value = clean,
                valueLower = lower,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    /** One-time seeding. Call this once on app startup (or after DB creation). */
    suspend fun seedDefaultsIfEmpty(type: SuggestionType, defaults: List<String>) {
        // If at least 1 exists, skip seeding
        val existing = dao.searchPrefix(type, prefix = "", limit = 1)
        if (existing.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val items = defaults
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .map { v ->
                val lower = v.lowercase()
                SuggestionEntity(
                    key = "${type.name}:$lower",
                    type = type,
                    value = v,
                    valueLower = lower,
                    createdAt = now
                )
            }

        dao.insertAll(items)
    }

    suspend fun addFromTriples(
        type: SuggestionType,
        values: List<String?>
    ) {
        values
            .mapNotNull { it?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .forEach { value ->
                add(type, value)
            }
    }
}