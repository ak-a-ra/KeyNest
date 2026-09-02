package com.example.core.model

import java.util.UUID

data class ProviderKeyItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "Default",
    val apiKey: String = "",
    val secretKey: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
