package com.example.data.repository

import com.example.data.db.ApiKeyDao
import com.example.data.model.ApiKeyItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class FakeApiKeyDao : ApiKeyDao {
    private val keysFlow = MutableStateFlow<List<ApiKeyItem>>(emptyList())
    var keys = mutableListOf<ApiKeyItem>()

    fun emit(newKeys: List<ApiKeyItem>) {
        keys = newKeys.toMutableList()
        keysFlow.value = keys
    }

    override fun getAllKeys(): Flow<List<ApiKeyItem>> = keysFlow
    override fun searchKeys(query: String): Flow<List<ApiKeyItem>> = flowOf(emptyList())
    override fun getKeyById(id: Long): Flow<ApiKeyItem?> = flowOf(null)
    override suspend fun insertKey(item: ApiKeyItem): Long {
        keys.add(item)
        keysFlow.value = keys.toList()
        return item.id
    }
    override suspend fun insertAllKeys(items: List<ApiKeyItem>) {}
    override suspend fun updateKey(item: ApiKeyItem) {}
    override suspend fun deleteKey(item: ApiKeyItem) {}
    override suspend fun deleteKeyById(id: Long) {}
    override suspend fun togglePin(id: Long, isPinned: Boolean) {}
    override suspend fun recordCopy(id: Long, timestamp: Long) {}
    override fun getKeyCount(): Flow<Int> = flowOf(keys.size)
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApiKeyRepositoryTest {

    @Test
    fun decryptionCache_cachesValues_andAvoidsRedundantDecryption() = runBlocking {
        val fakeDao = FakeApiKeyDao()
        val repository = ApiKeyRepository(fakeDao)
        
        val plainText = "sk-test-12345"
        
        val realEncryptedItem = ApiKeyItem(
            id = 1L,
            title = "Test",
            apiKey = com.example.data.security.Cryptography.encrypt(plainText),
            secretKey = "",
            provider = "Stripe",
            category = "Payments",
            environment = "Test",
            colorHex = "#ffffff"
        )

        // Emit first time
        fakeDao.emit(listOf(realEncryptedItem))

        val firstEmission = repository.allKeys.first()
        assertEquals("First emission should decrypt successfully", plainText, firstEmission.first().apiKey)

        // Emit again to trigger cache lookup (the identical ciphertext string is sent again)
        fakeDao.emit(listOf(realEncryptedItem))
        val secondEmission = repository.allKeys.first()
        assertEquals("Second emission should resolve from cache successfully", plainText, secondEmission.first().apiKey)
    }
}
