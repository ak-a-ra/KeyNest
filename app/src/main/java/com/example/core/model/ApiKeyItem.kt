package com.example.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val apiKey: String,
    val secretKey: String = "",
    val provider: String = "Other",
    val category: String = "AI & LLMs",
    val environment: String = "Production",
    val endpointUrl: String = "",
    val organizationId: String = "",
    val modelOrProject: String = "",
    val notes: String = "",
    val tags: String = "",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val copyCount: Int = 0,
    val lastCopiedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val rotationDays: Int? = null,
    val colorHex: String = "#FFB703"
)
