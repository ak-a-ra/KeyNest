package com.example.core.repository

import com.example.core.database.ApiKeyDao
import com.example.core.model.ApiKeyItem
import com.example.core.security.KeystoreCipher
import com.example.core.security.SecretCipher
import com.example.core.security.SecretCipherException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

/** Shown in place of a secret whose ciphertext cannot be decrypted (e.g. invalidated Keystore key). */
const val UNDECRYPTABLE_PLACEHOLDER = "<undecryptable>"

/**
 * Shared decryption cache and placeholder handler for repository layers.
 */
class CachedSecretCipher(
    private val cipher: SecretCipher = KeystoreCipher,
    private val maxCacheSize: Int = 128
) {
    private val lock = Any()
    private val decryptCache = object : LinkedHashMap<String, String>(maxCacheSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
            return size > maxCacheSize
        }
    }

    fun decryptOrPlaceholder(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        synchronized(lock) {
            decryptCache[cipherText]?.let { return it }
        }
        val decrypted = try {
            cipher.decrypt(cipherText)
        } catch (_: SecretCipherException) {
            UNDECRYPTABLE_PLACEHOLDER
        }
        synchronized(lock) {
            decryptCache[cipherText] = decrypted
        }
        return decrypted
    }

    fun clearCache() {
        synchronized(lock) {
            decryptCache.clear()
        }
    }

    fun encrypt(plainText: String): String =
        if (plainText.isEmpty()) "" else cipher.encrypt(plainText)
}

class ApiKeyRepository(
    private val dao: ApiKeyDao,
    private val cipher: SecretCipher = KeystoreCipher,
) {
    private val cryptor = CachedSecretCipher(cipher)

    fun clearCache() {
        cryptor.clearCache()
    }

    private fun String.decryptOrPlaceholder(): String = cryptor.decryptOrPlaceholder(this)
    private fun encrypt(plainText: String): String = cryptor.encrypt(plainText)

    suspend fun softDeleteKey(id: Long, timestamp: Long = System.currentTimeMillis()): Long {
        dao.softDeleteKey(id, timestamp)
        return id
    }

    suspend fun restoreKey(id: Long): Boolean {
        dao.restoreKey(id)
        return true
    }

    suspend fun permanentDeleteKey(id: Long): Boolean {
        dao.permanentDeleteKey(id)
        return true
    }

    private fun ApiKeyItem.decrypted() = copy(
        apiKey = apiKey.decryptOrPlaceholder(),
        secretKey = secretKey.decryptOrPlaceholder()
    )

    private fun ApiKeyItem.encrypted() = copy(
        apiKey = encrypt(apiKey),
        secretKey = encrypt(secretKey)
    )

    val allKeys: Flow<List<ApiKeyItem>> = dao.getAllKeys().map { list -> list.map { it.decrypted() } }
    val trashedKeys: Flow<List<ApiKeyItem>> = dao.getTrashedKeys().map { list -> list.map { it.decrypted() } }
    val trashCount: Flow<Int> = dao.getTrashCount()

    suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    fun searchKeys(query: String): Flow<List<ApiKeyItem>> = dao.searchKeys(query).map { list -> list.map { it.decrypted() } }

    fun getKeyById(id: Long): Flow<ApiKeyItem?> = dao.getKeyById(id).map { it?.decrypted() }

    suspend fun insertKey(item: ApiKeyItem): Long = dao.insertKey(item.encrypted())

    suspend fun saveKey(item: ApiKeyItem): Long {
        return if (item.id == 0L) {
            insertKey(item)
        } else {
            updateKey(item)
            item.id
        }
    }

    suspend fun insertAll(items: List<ApiKeyItem>) = dao.insertAllKeys(items.map { it.encrypted() })

    /** Atomic via @Transaction on the DAO — no window where the vault is empty. */
    suspend fun replaceAll(items: List<ApiKeyItem>) = dao.replaceAllKeys(items.map { it.encrypted() })

    suspend fun updateKey(item: ApiKeyItem) = dao.updateKey(item.encrypted())

    suspend fun deleteKey(item: ApiKeyItem) = dao.deleteKey(item)

    suspend fun deleteKeyById(id: Long) = dao.deleteKeyById(id)

    suspend fun togglePin(id: Long, isPinned: Boolean) = dao.togglePin(id, isPinned)

    suspend fun recordCopy(id: Long, timestamp: Long = System.currentTimeMillis()) = dao.recordCopy(id, timestamp)
}
