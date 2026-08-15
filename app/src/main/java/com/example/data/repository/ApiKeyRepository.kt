package com.example.data.repository

import com.example.data.db.ApiKeyDao
import com.example.data.model.ApiKeyItem
import com.example.data.security.Cryptography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class ApiKeyRepository(private val dao: ApiKeyDao) {

    private val decryptionCache = ConcurrentHashMap<String, String>()

    private fun decryptCached(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return decryptionCache.getOrPut(cipherText) { Cryptography.decrypt(cipherText) }
    }

    private fun encryptCached(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val cipherText = Cryptography.encrypt(plainText)
        decryptionCache[cipherText] = plainText
        return cipherText
    }

    private fun ApiKeyItem.decrypted() = copy(
        apiKey = decryptCached(apiKey),
        secretKey = decryptCached(secretKey)
    )

    private fun ApiKeyItem.encrypted() = copy(
        apiKey = encryptCached(apiKey),
        secretKey = encryptCached(secretKey)
    )

    val allKeys: Flow<List<ApiKeyItem>> = dao.getAllKeys().map { list -> list.map { it.decrypted() } }

    fun searchKeys(query: String): Flow<List<ApiKeyItem>> = dao.searchKeys(query).map { list -> list.map { it.decrypted() } }

    fun getKeyById(id: Long): Flow<ApiKeyItem?> = dao.getKeyById(id).map { it?.decrypted() }

    suspend fun insertKey(item: ApiKeyItem): Long = dao.insertKey(item.encrypted())

    suspend fun insertAll(items: List<ApiKeyItem>) = dao.insertAllKeys(items.map { it.encrypted() })


    suspend fun updateKey(item: ApiKeyItem) = dao.updateKey(item.encrypted())

    suspend fun deleteKey(item: ApiKeyItem) = dao.deleteKey(item)

    suspend fun deleteKeyById(id: Long) = dao.deleteKeyById(id)

    suspend fun togglePin(id: Long, isPinned: Boolean) = dao.togglePin(id, isPinned)

    suspend fun recordCopy(id: Long, timestamp: Long = System.currentTimeMillis()) = dao.recordCopy(id, timestamp)
}
