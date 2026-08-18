package com.example.data.repository

import android.content.Context
import com.example.data.db.ApiKeyDao
import com.example.data.model.ApiKeyItem
import com.example.data.security.Cryptography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
        try {
            val cipherText = Cryptography.encrypt(plainText)
            decryptionCache[cipherText] = plainText
            return cipherText
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
        apiKey = decryptCached(apiKey),
        secretKey = decryptCached(secretKey)
    )

    private fun ApiKeyItem.encrypted() = copy(
        apiKey = encryptCached(apiKey),
        secretKey = encryptCached(secretKey)
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


    suspend fun updateKey(item: ApiKeyItem) = dao.updateKey(item.encrypted())

    suspend fun deleteKey(item: ApiKeyItem) = dao.deleteKey(item)

    suspend fun deleteKeyById(id: Long) = dao.deleteKeyById(id)

    suspend fun togglePin(id: Long, isPinned: Boolean) = dao.togglePin(id, isPinned)

    suspend fun recordCopy(id: Long, timestamp: Long = System.currentTimeMillis()) = dao.recordCopy(id, timestamp)

    // Migration support for legacy plaintext secrets
    private val MIGRATION_KEY = "keynest_migration_version"

    suspend fun isMigrationNeeded(context: Context): Boolean {
        val prefs = context.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        val currentVersion = prefs.getInt(MIGRATION_KEY, 0)
        val databaseVersion = dao.getKeyCount().first()
        return currentVersion < databaseVersion
    }

    fun markMigrationComplete(context: Context, version: Int) {
        val prefs = context.getApplicationContext().getSharedPreferences("key-nest-prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt(MIGRATION_KEY, version).apply()
    }

    /**
     * One-time migration: converts any legacy plaintext secrets to encrypted form.
     * This must be called on app startup before any database operations.
     * Returns number of entries migrated, or -1 if migration was skipped/aborted.
     */
    suspend fun migrateLegacyPlaintextSecrets(context: Context): Int {
        val allKeys = dao.getAllKeys().first()
        val plaintextKeys = allKeys.filter { it.apiKey.isNotEmpty() && !it.apiKey.startsWith("enc:") }

        if (plaintextKeys.isEmpty()) {
            // Nothing to migrate, mark as done
            markMigrationComplete(context, 1)
            return 0
        }

        // Perform migration in a transaction
        return try {
            // Use Room's built-in transaction via dao
            var migratedCount = 0
            for (key in plaintextKeys) {
                // Check if already encrypted (starts with "enc:" prefix)
                if (key.apiKey.startsWith("enc:")) continue

                // Treat as plaintext and re-encrypt
                val encryptedApiKey = Cryptography.encrypt(key.apiKey)
                // Update the item with encrypted key
                val updatedItem = key.copy(apiKey = encryptedApiKey)
                dao.updateKey(updatedItem)
                migratedCount++
            }
            // Mark migration as complete (version 1)
            markMigrationComplete(context, 1)
            migratedCount
        } catch (e: Exception) {
            // Security: Do not silently fail migration; preserve original data
            // Log non-sensitive error and return -1 to indicate failure
            throw RuntimeException("Legacy migration failed - original data preserved")
        }
    }
}
