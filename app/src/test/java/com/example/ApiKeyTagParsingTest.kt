package com.example

import com.example.core.util.ApiKeyFormatting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiKeyTagParsingTest {

    @Test
    fun parseTags_handlesCommaSeparatedAndHashes() {
        val raw = " #prod ,  staging, tag:clientA , billing "
        val result = ApiKeyFormatting.parseTags(raw)
        assertEquals(listOf("prod", "staging", "clientA", "billing"), result)
    }

    @Test
    fun parseTags_handlesEmptyAndBlanks() {
        val result = ApiKeyFormatting.parseTags("   , ,  # ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun parseTags_deduplicatesTags() {
        val raw = "prod, PROD, prod"
        val result = ApiKeyFormatting.parseTags(raw)
        assertEquals(listOf("prod"), result)
    }

    @Test
    fun formatTags_joinsCleanly() {
        val tags = listOf("prod", "client-a", "billing")
        val formatted = ApiKeyFormatting.formatTags(tags)
        assertEquals("prod, client-a, billing", formatted)
    }
}
