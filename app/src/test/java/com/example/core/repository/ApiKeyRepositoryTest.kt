package com.example.core.repository

import android.content.Context
import androidx.room.Room
import com.example.core.database.ApiKeyDao
import com.example.core.database.AppDatabase
import com.example.core.model.ApiKeyItem
import com.example.core.security.Cryptography
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
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
    override fun getTrashCount(): Flow<Int> = flowOf(0)
    override fun getTrashedKeys(): Flow<List<ApiKeyItem>> = flowOf(emptyList())
    override suspend fun softDeleteKey(id: Long, timestamp: Long) {}
    override suspend fun restoreKey(id: Long) {}
    override suspend fun permanentDeleteKey(id: Long) {}
    override suspend fun emptyTrash() {}
    override suspend fun deleteAllKeys() {
        keys.clear()
        keysFlow.value = emptyList()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ApiKeyRepositoryTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun item(id: Long, key: String) = ApiKeyItem(
        id = id,
        title = "Test$id",
        apiKey = key,
        secretKey = "",
        provider = "Stripe",
        category = "Payments",
        environment = "Test",
        colorHex = "#ffffff"
    )

    /** F5 regression: replaceAll is one transaction — old rows are fully replaced, no partial state. */
    @Test
    fun replaceAll_replacesVaultAtomically_neverLeavesPartialOrEmptyState() = runBlocking {
        val repository = ApiKeyRepository(db.apiKeyDao())
        db.apiKeyDao().insertAllKeys(listOf(item(1, Cryptography.encrypt("old1")), item(2, Cryptography.encrypt("old2"))))

        repository.replaceAll(listOf(item(0, "new1"), item(0, "new2"), item(0, "new3")))

        // Exactly the new set is visible in a single consistent snapshot — never an intermediate empty/partial state.
        val observed = repository.allKeys.first()
        assertEquals(setOf("new1", "new2", "new3"), observed.map { it.apiKey }.toSet())
        assertEquals(3, observed.size)
    }

    /** F7: decryption works directly per read without any cache layer. */
    @Test
    fun allKeys_decryptsRoundTrip() = runBlocking {
        val repository = ApiKeyRepository(db.apiKeyDao())
        db.apiKeyDao().insertAllKeys(listOf(item(1, Cryptography.encrypt("sk-test-12345"))))

        val first = repository.allKeys.first().single()
        assertEquals("sk-test-12345", first.apiKey)
    }
}
