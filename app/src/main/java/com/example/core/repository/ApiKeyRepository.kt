package com.example.core.repository

import com.example.core.database.ApiKeyDao
import com.example.core.model.ApiKeyItem
import com.example.core.security.Cryptography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ApiKeyRepository(private val dao: ApiKeyDao) {

    private fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        return Cryptography.decrypt(cipherText)
    }

    private fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        try {
            return Cryptography.encrypt(plainText)
        } catch (e: RuntimeException) {
            // Security: Do not silently persist empty/replaced secret on encryption failure
            throw e
        }
    }

    suspend fun softDeleteKey(id: Long, timestamp: Long = System.currentTimeMillis()): Long {
        try {
            dao.softDeleteKey(id, timestamp)
            return id
        } catch (e: Exception) {
            // Security: Do not silently fail soft-delete; propagate for UI handling
            throw RuntimeException("Soft-delete failed")
        }
    }

    suspend fun restoreKey(id: Long): Boolean {
        try {
            dao.restoreKey(id)
            return true
        } catch (e: Exception) {
            // Security: Do not silently fail restore; propagate for UI handling
            throw RuntimeException("Restore failed")
        }
    }

    suspend fun permanentDeleteKey(id: Long): Boolean {
        try {
            dao.permanentDeleteKey(id)
            return true
        } catch (e: Exception) {
            // Security: Do not silently fail permanent delete; propagate for UI handling
            throw RuntimeException("Permanent delete failed")
        }
    }

    private fun ApiKeyItem.decrypted() = copy(
        apiKey = decrypt(apiKey),
        secretKey = decrypt(secretKey)
    )

    private fun ApiKeyItem.encrypted() = copy(
        apiKey = encrypt(apiKey),
        secretKey = encrypt(secretKey)
    )

    val allKeys: Flow<List<ApiKeyItem>> = dao.getAllKeys().map { list -> list.map { it.decrypted() } }
    val trashedKeys: Flow<List<ApiKeyItem>> = dao.getTrashedKeys().map { list -> list.map { it.decrypted() } }
    val trashCount: Flow<Int> = dao.getTrashCount()

    suspend fun emptyTrash() {
        try {
            dao.emptyTrash()
        } catch (e: Exception) {
            throw RuntimeException("Empty trash failed")
        }
    }

    fun searchKeys(query: String): Flow<List<ApiKeyItem>> = dao.searchKeys(query).map { list -> list.map { it.decrypted() } }

    fun getKeyById(id: Long): Flow<ApiKeyItem?> = dao.getKeyById(id).map { it?.decrypted() }

    suspend fun insertKey(item: ApiKeyItem): Long = dao.insertKey(item.encrypted())

    suspend fun insertAll(items: List<ApiKeyItem>) = dao.insertAllKeys(items.map { it.encrypted() })

    /** Atomic via @Transaction on the DAO — no window where the vault is empty. */
    suspend fun replaceAll(items: List<ApiKeyItem>) = dao.replaceAllKeys(items.map { it.encrypted() })

    suspend fun updateKey(item: ApiKeyItem) = dao.updateKey(item.encrypted())

    suspend fun deleteKey(item: ApiKeyItem) = dao.deleteKey(item)

    suspend fun deleteKeyById(id: Long) = dao.deleteKeyById(id)

    suspend fun togglePin(id: Long, isPinned: Boolean) = dao.togglePin(id, isPinned)

    suspend fun recordCopy(id: Long, timestamp: Long = System.currentTimeMillis()) = dao.recordCopy(id, timestamp)
}
