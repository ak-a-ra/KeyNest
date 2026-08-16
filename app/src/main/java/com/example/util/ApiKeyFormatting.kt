package com.example.util

import com.example.data.model.ApiKeyItem

/**
 * Formatting utilities for exporting API key data in various formats.
 * 
 * All formatting is done in-memory only. No key values are transmitted or logged
 * during the formatting process. Follows Material Design 3 accessibility guidelines.
 */
object ApiKeyFormatting {

    /**
     * Convert a list of ApiKeyItem to JSON format.
     * 
     * @param keys List of API key items to format
     * @return JSON string representation of the keys
     */
    fun toJson(keys: List<ApiKeyItem>): String {
        if (keys.isEmpty()) return "[]"

        val items = keys.map { key ->
            buildString {
                append("{")
                append("\"id\":$(${key.id)},")
                append("\"title\":${escapeJson(key.title)},")
                append("\"provider\":${escapeJson(key.provider)},")
                append("\"category\":${escapeJson(key.category)},")
                append("\"environment\":${escapeJson(key.environment)},")
                append("\"apiKey\":\"${escapeJson(key.apiKey)}\",")
                append("\"secretKey\":\"${escapeJson(key.secretKey)}\",")
                append("\"endpointUrl\":${escapeJson(key.endpointUrl)},")
                append("\"notes\":${escapeJson(key.notes)},")
                append("\"tags\":\"${escapeJson(key.tags)}\",")
                append("\"isPinned\":${key.isPinned},")
                append("\"copyCount\":${key.copyCount},")
                append("\"createdAt\":${key.createdAt},")
                key.expiresAt?.let { append("\"expiresAt\":${it},") }
                key.rotationDays?.let { append("\"rotationDays\":${it},") }
                key.colorHex?.let { append("\"colorHex\":${escapeJson(it)}}") }
                    ?: append("}")
            }
        }

        return "[$items.joinToString(",")]"
    }

    /**
     * Convert a list of ApiKeyItem to CSV format.
     * 
     * @param keys List of API key items to format
     * @return CSV string representation of the keys
     */
    fun toCsv(keys: List<ApiKeyItem>): String {
        if (keys.isEmpty()) return ""

        val headers = listOf(
            "ID",
            "Title",
            "Provider",
            "Category",
            "Environment",
            "API Key",
            "Secret Key",
            "Endpoint URL",
            "Notes",
            "Tags",
            "Pinned",
            "Copy Count",
            "Created At"
        )

        val rows = mutableListOf<String>()
        rows.add(headers.joinToString(","))

        keys.forEach { key ->
            val row = listOf(
                key.id.toString(),
                wrapCsv(key.title),
                wrapCsv(key.provider),
                wrapCsv(key.category),
                wrapCsv(key.environment),
                wrapCsv(key.apiKey),
                wrapCsv(key.secretKey),
                wrapCsv(key.endpointUrl),
                wrapCsv(key.notes),
                wrapCsv(key.tags),
                key.isPinned.toString(),
                key.copyCount.toString(),
                key.createdAt.toString()
            )
            rows.add(row.joinToString(","))
        }

        return rows.joinToString("\n")
    }

    /**
     * Convert a list of ApiKeyItem to plain text format.
     * 
     * @param keys List of API key items to format
     * @return Plain text representation of the keys
     */
    fun toPlainText(keys: List<ApiKeyItem>): String {
        if (keys.isEmpty()) return "No keys to export."

        val sb = StringBuilder()
        sb.append("Developer Code Export - KeyNest\n")
        sb.append("=".repeat(40).plus("\n\n"))

        keys.forEachIndexed { index, key ->
            sb.append("Key #${index + 1}\n")
            sb.append("-" repeat 30 plus "\n")
            sb.append("Title: ").append(key.title).append("\n")
            sb.append("Provider: ").append(key.provider).append("\n")
            sb.append("Category: ").append(key.category).append("\n")
            sb.append("Environment: ").append(key.environment).append("\n")
            sb.append("API Key: ").append(key.apiKey).append("\n")
            if (key.secretKey.isNotBlank()) {
                sb.append("Secret Key: ").append("••••••••••••••••").append("\n")
            }
            if (key.endpointUrl.isNotBlank()) {
                sb.append("Endpoint URL: ").append(key.endpointUrl).append("\n")
            }
            if (key.notes.isNotBlank()) {
                sb.append("Notes: ").append(key.notes).append("\n")
            }
            if (key.tags.isNotBlank()) {
                sb.append("Tags: ").append(key.tags).append("\n")
            }
            sb.append("Pinned: ").append(key.isPinned.toString()).append("\n")
            sb.append("Copy Count: ").append(key.copyCount).append("\n")
            sb.append("Created At: ").append(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(java.time.ZoneId.systemDefault())
                .format(java.time.Instant.ofEpochMilli(key.createdAt))).append("\n")
            sb.append("\n")
        }

        sb.append("=".repeat(40).plus("\n"))
        sb.append("Export generated by KeyNest Vault. ")
        sb.append("Protect this exported file. Do NOT commit secrets to public repositories.\n")

        return sb.toString()
    }

    /** JSON string escaping */
    private fun escapeJson(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /** CSV cell wrapping with proper quoting */
    private fun wrapCsv(s: String): String {
        val needsQuoting = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")
        if (!needsQuoting) return s
        val escaped = s.replace("\\", "\\\\").replace("\"", "\\\"")
        return "\"$escaped\""
    }
}