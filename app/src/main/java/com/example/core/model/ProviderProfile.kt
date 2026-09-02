package com.example.core.model

data class ProviderProfile(
    val id: String,
    val category: String = "AI & LLMs",
    val displayName: String,
    val baseUrl: String = "",
    val customHeadersJson: String = "{}",
    val isActive: Boolean = true,
    val keys: List<ProviderKeyItem> = emptyList(),
    val activeKeyId: String = "",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val copyCount: Int = 0,
    val lastCopiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val colorHex: String = "#10A37F",
    val notes: String = "",
    val tags: String = ""
) {
    val activeKey: ProviderKeyItem?
        get() {
            if (activeKeyId.isNotEmpty()) {
                val found = keys.find { it.id == activeKeyId }
                if (found != null) return found
            }
            return keys.firstOrNull { it.isPrimary } ?: keys.firstOrNull()
        }

    val activeApiKey: String
        get() = activeKey?.apiKey.orEmpty()

    val isConfigured: Boolean
        get() = keys.isNotEmpty() && activeApiKey.isNotBlank()

    val totalKeysCount: Int
        get() = keys.size
}
