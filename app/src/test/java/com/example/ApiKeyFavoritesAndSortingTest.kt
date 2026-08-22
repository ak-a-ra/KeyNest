package com.example

import com.example.core.model.ApiKeyItem
import com.example.feature.vault.SortOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyFavoritesAndSortingTest {

    private fun sampleKey(id: Long, title: String, isPinned: Boolean, copyCount: Int = 0, createdAt: Long = id): ApiKeyItem {
        return ApiKeyItem(
            id = id,
            title = title,
            provider = "OpenAI",
            category = "AI & LLMs",
            environment = "Production",
            apiKey = "sk-test-$id",
            isPinned = isPinned,
            copyCount = copyCount,
            createdAt = createdAt
        )
    }

    @Test
    fun pinnedKeys_alwaysSortedFirst_inRecentSort() {
        val key1 = sampleKey(1, "Alpha", isPinned = false, createdAt = 1000)
        val key2 = sampleKey(2, "Beta", isPinned = true, createdAt = 500)
        val key3 = sampleKey(3, "Gamma", isPinned = false, createdAt = 2000)
        val key4 = sampleKey(4, "Delta", isPinned = true, createdAt = 800)

        val list = listOf(key1, key2, key3, key4)
        val sorted = list.sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenByDescending { it.createdAt })

        assertEquals(listOf(key4, key2, key3, key1), sorted)
        assertTrue(sorted[0].isPinned)
        assertTrue(sorted[1].isPinned)
        assertTrue(!sorted[2].isPinned)
        assertTrue(!sorted[3].isPinned)
    }

    @Test
    fun pinnedKeys_alwaysSortedFirst_inAlphabeticalSort() {
        val key1 = sampleKey(1, "Zeta", isPinned = true)
        val key2 = sampleKey(2, "Alpha", isPinned = false)
        val key3 = sampleKey(3, "Beta", isPinned = true)

        val list = listOf(key1, key2, key3)
        val sorted = list.sortedWith(compareByDescending<ApiKeyItem> { it.isPinned }.thenBy { it.title.lowercase() })

        assertEquals(listOf(key3, key1, key2), sorted)
    }

    @Test
    fun onlyFavorites_filtersCorrectly() {
        val key1 = sampleKey(1, "Key 1", isPinned = true)
        val key2 = sampleKey(2, "Key 2", isPinned = false)
        val key3 = sampleKey(3, "Key 3", isPinned = true)

        val list = listOf(key1, key2, key3)
        val onlyFavorites = list.filter { it.isPinned }

        assertEquals(listOf(key1, key3), onlyFavorites)
        assertEquals(2, onlyFavorites.size)
    }

    @Test
    fun sampleKeyWithTags_parsesCorrectly() {
        val key = ApiKeyItem(
            title = "OpenAI",
            provider = "OpenAI",
            category = "AI & LLMs",
            environment = "Production",
            apiKey = "sk-proj-12345",
            tags = "prod, gpt4, llm"
        )
        val tags = com.example.core.util.ApiKeyFormatting.parseTags(key.tags)
        assertEquals(listOf("prod", "gpt4", "llm"), tags)
    }
}
