package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_profiles")
data class ProviderProfileEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val displayName: String,
    val baseUrl: String,
    val customHeadersJson: String = "{}",
    val isActive: Boolean = true,
    val keysJson: String,
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
)
