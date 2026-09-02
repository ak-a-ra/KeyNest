package com.example.core.util

import com.example.core.model.ApiKeyItem

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

    /** Export keys to JSON format string */
    fun toJson(keys: List<ApiKeyItem>): String {
        val sb = java.lang.StringBuilder()
        sb.append("[\n")
        val validKeys = keys.filter { !it.isDeleted }
        validKeys.forEachIndexed { index, item ->
            sb.append("  {\n")
            sb.append("    \"id\": ").append(item.id).append(",\n")
            sb.append("    \"title\": \""); appendEscapedJson(sb, item.title); sb.append("\",\n")
            sb.append("    \"apiKey\": \""); appendEscapedJson(sb, item.apiKey); sb.append("\",\n")
            sb.append("    \"secretKey\": \""); appendEscapedJson(sb, item.secretKey); sb.append("\",\n")
            sb.append("    \"provider\": \""); appendEscapedJson(sb, item.provider); sb.append("\",\n")
            sb.append("    \"category\": \""); appendEscapedJson(sb, item.category); sb.append("\",\n")
            sb.append("    \"environment\": \""); appendEscapedJson(sb, item.environment); sb.append("\",\n")
            sb.append("    \"endpointUrl\": \""); appendEscapedJson(sb, item.endpointUrl); sb.append("\",\n")
            sb.append("    \"organizationId\": \""); appendEscapedJson(sb, item.organizationId); sb.append("\",\n")
            sb.append("    \"modelOrProject\": \""); appendEscapedJson(sb, item.modelOrProject); sb.append("\",\n")
            sb.append("    \"notes\": \""); appendEscapedJson(sb, item.notes); sb.append("\",\n")
            sb.append("    \"tags\": \""); appendEscapedJson(sb, item.tags); sb.append("\"\n")
            sb.append("  }")
            if (index < validKeys.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    /** Export keys to CSV format string */
    fun toCsv(keys: List<ApiKeyItem>): String {
        val sb = java.lang.StringBuilder()
        sb.append("Title,Provider,Category,Environment,API Key,Secret Key,Endpoint URL,Org ID,Model/Project,Tags,Notes\n")
        fun escapeCsv(value: String): String {
            return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
                "\"" + value.replace("\"", "\"\"") + "\""
            } else {
                value
            }
        }
        keys.filter { !it.isDeleted }.forEach { item ->
            sb.append(escapeCsv(item.title)).append(",")
                .append(escapeCsv(item.provider)).append(",")
                .append(escapeCsv(item.category)).append(",")
                .append(escapeCsv(item.environment)).append(",")
                .append(escapeCsv(item.apiKey)).append(",")
                .append(escapeCsv(item.secretKey)).append(",")
                .append(escapeCsv(item.endpointUrl)).append(",")
                .append(escapeCsv(item.organizationId)).append(",")
                .append(escapeCsv(item.modelOrProject)).append(",")
                .append(escapeCsv(item.tags)).append(",")
                .append(escapeCsv(item.notes)).append("\n")
        }
        return sb.toString().trimEnd()
    }

    /** Export keys to human-readable plain text format */
    fun toPlainText(keys: List<ApiKeyItem>): String {
        val sb = java.lang.StringBuilder()
        keys.filter { !it.isDeleted }.forEachIndexed { index, item ->
            sb.append("Title: ").append(item.title).append("\n")
            sb.append("Provider: ").append(item.provider).append("\n")
            sb.append("Environment: ").append(item.environment).append("\n")
            sb.append("API Key: ").append(item.apiKey).append("\n")
            if (item.secretKey.isNotBlank()) {
                sb.append("Secret Key: ").append(item.secretKey).append("\n")
            }
            if (item.endpointUrl.isNotBlank()) {
                sb.append("Endpoint URL: ").append(item.endpointUrl).append("\n")
            }
            if (item.tags.isNotBlank()) {
                sb.append("Tags: ").append(item.tags).append("\n")
            }
            if (item.notes.isNotBlank()) {
                sb.append("Notes: ").append(item.notes).append("\n")
            }
            if (index < keys.size - 1) {
                sb.append("\n----------------------------------------\n\n")
            }
        }
        return sb.toString().trimEnd()
    }
}
