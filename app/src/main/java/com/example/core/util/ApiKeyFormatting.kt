package com.example.core.util

/**
 * Formatting utilities for tag processing and string manipulation in the vault.
 * 
 * All formatting is done in-memory only. No key values are transmitted or logged.
 */
object ApiKeyFormatting {

    /**
     * Fast check if a comma-separated tag string contains a target tag without intermediate list allocations.
     */
    fun hasTag(raw: String, targetTag: String?): Boolean {
        if (targetTag.isNullOrBlank() || raw.isBlank()) return false
        val cleanTarget = targetTag.trim().removePrefix("#").removePrefix("tag:").trim()
        var start = 0
        while (start < raw.length) {
            val commaIndex = raw.indexOf(',', start)
            val end = if (commaIndex == -1) raw.length else commaIndex
            val segment = raw.substring(start, end).trim().removePrefix("#").removePrefix("tag:").trim()
            if (segment.equals(cleanTarget, ignoreCase = true)) {
                return true
            }
            if (commaIndex == -1) break
            start = commaIndex + 1
        }
        return false
    }

    /**
     * Parse comma-separated raw tag string into normalized list of unique tags.
     * Trims whitespace, removes leading '#' or 'tag:', enforces max 20 chars per tag, and discards blanks.
     */
    fun parseTags(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(",")
            .asSequence()
            .map { it.trim().removePrefix("#").removePrefix("tag:").trim() }
            .filter { it.isNotBlank() }
            .map { it.take(20) }
            .distinctBy { it.lowercase() }
            .toList()
    }

    /**
     * Format a list of tags back into a clean comma-separated string.
     */
    fun formatTags(tags: List<String>): String = tags.asSequence()
        .map { it.trim().removePrefix("#").removePrefix("tag:").trim() }
        .filter { it.isNotBlank() }
        .map { it.take(20) }
        .distinctBy { it.lowercase() }
        .joinToString(", ")

    /** JSON string escaping with single-pass buffer writing */
    fun appendEscapedJson(sb: java.lang.StringBuilder, s: String) {
        for (i in 0 until s.length) {
            when (val c = s[i]) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
    }
}
