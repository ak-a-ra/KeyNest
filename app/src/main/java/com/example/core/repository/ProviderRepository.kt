package com.example.core.repository

import com.example.core.database.ProviderDao
import com.example.core.database.ProviderProfileEntity
import com.example.core.model.ProviderKeyItem
import com.example.core.model.ProviderProfile
import com.example.core.security.KeystoreCipher
import com.example.core.security.SecretCipher
import com.example.core.security.SecretCipherException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class ProviderRepository(
    private val dao: ProviderDao,
    private val cipher: SecretCipher = KeystoreCipher
) {
    private val cryptor = CachedSecretCipher(cipher)

    private fun String.decryptOrPlaceholder(): String = cryptor.decryptOrPlaceholder(this)
    private fun encrypt(plainText: String): String = cryptor.encrypt(plainText)

    private fun serializeAndEncryptKeys(keys: List<ProviderKeyItem>): String {
        val array = JSONArray()
        for (k in keys) {
            val obj = JSONObject().apply {
                put("id", k.id)
                put("label", k.label)
                put("apiKey", encrypt(k.apiKey))
                put("secretKey", encrypt(k.secretKey))
                put("isPrimary", k.isPrimary)
                put("createdAt", k.createdAt)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private val keysCache = ConcurrentHashMap<String, List<ProviderKeyItem>>()

    private fun decryptAndDeserializeKeys(jsonStr: String): List<ProviderKeyItem> {
        if (jsonStr.isBlank() || jsonStr == "[]") return emptyList()
        return keysCache.computeIfAbsent(jsonStr) { raw ->
            val result = mutableListOf<ProviderKeyItem>()
            try {
                val array = JSONArray(raw)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    result.add(
                        ProviderKeyItem(
                            id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                            label = obj.optString("label", "Default"),
                            apiKey = obj.optString("apiKey", "").decryptOrPlaceholder(),
                            secretKey = obj.optString("secretKey", "").decryptOrPlaceholder(),
                            isPrimary = obj.optBoolean("isPrimary", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (_: Exception) {}
            result
        }
    }

    private fun ProviderProfileEntity.toDomain(): ProviderProfile = ProviderProfile(
        id = id,
        category = category,
        displayName = displayName,
        baseUrl = baseUrl,
        customHeadersJson = customHeadersJson,
        isActive = isActive,
        keys = decryptAndDeserializeKeys(keysJson),
        activeKeyId = activeKeyId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        copyCount = copyCount,
        lastCopiedAt = lastCopiedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        colorHex = colorHex,
        notes = notes,
        tags = tags
    )

    private fun ProviderProfile.toEntity(): ProviderProfileEntity = ProviderProfileEntity(
        id = id,
        category = category,
        displayName = displayName,
        baseUrl = baseUrl,
        customHeadersJson = customHeadersJson,
        isActive = isActive,
        keysJson = serializeAndEncryptKeys(keys),
        activeKeyId = activeKeyId,
        isPinned = isPinned,
        isDeleted = isDeleted,
        deletedAt = deletedAt,
        copyCount = copyCount,
        lastCopiedAt = lastCopiedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        colorHex = colorHex,
        notes = notes,
        tags = tags
    )

    val allProviders: Flow<List<ProviderProfile>> = dao.getAllProviders().map { list ->
        list.map { it.toDomain() }
    }

    val trashedProviders: Flow<List<ProviderProfile>> = dao.getTrashedProviders().map { list ->
        list.map { it.toDomain() }
    }

    val trashCount: Flow<Int> = dao.getTrashCount()

    fun getProviderById(id: String): Flow<ProviderProfile?> = dao.getProviderById(id).map {
        it?.toDomain()
    }

    suspend fun saveProvider(profile: ProviderProfile) {
        val updated = profile.copy(updatedAt = System.currentTimeMillis())
        dao.insertProvider(updated.toEntity())
    }

    suspend fun insertAll(profiles: List<ProviderProfile>) {
        dao.insertAllProviders(profiles.map { it.toEntity() })
    }

    suspend fun replaceAll(profiles: List<ProviderProfile>) {
        dao.replaceAllProviders(profiles.map { it.toEntity() })
    }

    suspend fun togglePin(id: String, isPinned: Boolean) {
        dao.togglePin(id, isPinned)
    }

    suspend fun toggleActive(id: String, isActive: Boolean) {
        dao.toggleActive(id, isActive)
    }

    suspend fun recordCopy(id: String, timestamp: Long = System.currentTimeMillis()) {
        dao.recordCopy(id, timestamp)
    }

    suspend fun softDeleteProvider(id: String, timestamp: Long = System.currentTimeMillis()) {
        dao.softDeleteProvider(id, timestamp)
    }

    suspend fun restoreProvider(id: String) {
        dao.restoreProvider(id)
    }

    suspend fun permanentDeleteProvider(id: String) {
        dao.permanentDeleteProvider(id)
    }

    suspend fun emptyTrash() {
        dao.emptyTrash()
    }

    suspend fun setActiveKey(providerId: String, keyId: String) {
        val current = getProviderById(providerId).firstOrNull() ?: return
        val updatedKeys = current.keys.map { it.copy(isPrimary = (it.id == keyId)) }
        val updated = current.copy(
            keys = updatedKeys,
            activeKeyId = keyId,
            updatedAt = System.currentTimeMillis()
        )
        saveProvider(updated)
    }

    suspend fun addOrUpdateKey(providerId: String, keyItem: ProviderKeyItem) {
        val current = getProviderById(providerId).firstOrNull()
        if (current == null) return
        val existingIndex = current.keys.indexOfFirst { it.id == keyItem.id }
        val rawKeys = if (existingIndex >= 0) {
            current.keys.toMutableList().apply { set(existingIndex, keyItem) }
        } else {
            current.keys + keyItem
        }
        val newKeys = if (keyItem.isPrimary) {
            rawKeys.map { if (it.id == keyItem.id) it else it.copy(isPrimary = false) }
        } else {
            rawKeys
        }
        val activeId = if (keyItem.isPrimary || current.activeKeyId.isEmpty()) keyItem.id else current.activeKeyId
        val updated = current.copy(
            keys = newKeys,
            activeKeyId = activeId,
            updatedAt = System.currentTimeMillis()
        )
        saveProvider(updated)
    }

    suspend fun removeKey(providerId: String, keyId: String) {
        val current = getProviderById(providerId).firstOrNull() ?: return
        val newKeys = current.keys.filterNot { it.id == keyId }
        val newActiveId = if (current.activeKeyId == keyId) {
            newKeys.firstOrNull()?.id.orEmpty()
        } else current.activeKeyId

        val updated = current.copy(
            keys = newKeys,
            activeKeyId = newActiveId,
            updatedAt = System.currentTimeMillis()
        )
        saveProvider(updated)
    }
}
