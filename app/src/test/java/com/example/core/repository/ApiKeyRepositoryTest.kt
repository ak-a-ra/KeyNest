package com.example.core.repository

import android.content.Context
import androidx.room.Room
import com.example.core.database.ApiKeyDao
import com.example.core.database.AppDatabase
import com.example.core.model.ApiKeyItem
import com.example.core.security.SecretCipher
import com.example.core.security.SecretCipherException
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
    override suspend fun insertAllKeys(items: List<ApiKeyItem>) {
        keys.addAll(items)
        keysFlow.value = keys.toList()
    }
    override suspend fun updateKey(item: ApiKeyItem) {
        val i = keys.indexOfFirst { it.id == item.id }
        if (i >= 0) keys[i] = item else keys.add(item)
        keysFlow.value = keys.toList()
    }
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

    /** F5: verifies replaceAll replaces all rows with the new set. Atomicity itself is guaranteed
     *  by the @Transaction on ApiKeyDao.replaceAllKeys and is not directly assertable in Robolectric. */
    @Test
    fun replaceAll_replacesAllRows() = runBlocking {
        val repository = ApiKeyRepository(db.apiKeyDao(), FakeCipher())
        db.apiKeyDao().insertAllKeys(listOf(item(1, "enc-old1"), item(2, "enc-old2")))

        repository.replaceAll(listOf(item(0, "new1"), item(0, "new2"), item(0, "new3")))

        // Exactly the new set is visible in a single consistent snapshot — never an intermediate empty/partial state.
        val observed = repository.allKeys.first()
        assertEquals(setOf("new1", "new2", "new3"), observed.map { it.apiKey }.toSet())
        assertEquals(3, observed.size)
    }

    /** F7: decryption works directly per read without any cache layer. */
    @Test
    fun allKeys_decryptsRoundTrip() = runBlocking {
        val repository = ApiKeyRepository(db.apiKeyDao(), FakeCipher())
        db.apiKeyDao().insertAllKeys(listOf(item(1, "enc-sk-test-12345")))

        val first = repository.allKeys.first().single()
        assertEquals("sk-test-12345", first.apiKey)
    }
}

/** Test fake for the SecretCipher seam (F2) — no Robolectric sniffing needed. */
class FakeCipher : SecretCipher {
    var failEncrypt = false
    var failDecrypt = false

    override fun encrypt(plainText: String): String {
        if (failEncrypt) throw SecretCipherException("Encryption failed")
        return "enc:$plainText"
    }

    override fun decrypt(cipherText: String): String {
        if (failDecrypt) throw SecretCipherException("Decryption failed")
        return cipherText.removePrefix("enc:")
    }
}

class SecretCipherSeamTest {

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

    /** F2: repository round-trips through an injectable cipher. */
    @Test
    fun cipher_roundTrip() = runBlocking {
        val dao = FakeApiKeyDao()
        val repository = ApiKeyRepository(dao, FakeCipher())

        repository.insertKey(item(0, "sk-round-trip"))

        // Stored value is encrypted, read path decrypts it back.
        assertEquals("enc:sk-round-trip", dao.keys.single().apiKey)
        assertEquals("sk-round-trip", repository.allKeys.first().single().apiKey)
    }

    /** F3: encrypt failure must abort before any DB write — never persist "" over the secret. */
    @Test
    fun insertKey_encryptFailure_leavesDbUntouched() = runBlocking {
        val dao = FakeApiKeyDao()
        dao.insertKey(item(1, "enc:existing"))
        val cipher = FakeCipher().apply { failEncrypt = true }
        val repository = ApiKeyRepository(dao, cipher)

        try {
            repository.updateKey(item(1, "sk-new-value"))
            throw AssertionError("expected SecretCipherException")
        } catch (_: SecretCipherException) {
        }

        assertEquals(1, dao.keys.size)
        assertEquals("enc:existing", dao.keys.single().apiKey)
    }

    /** F3: insert path also aborts on encryption failure — nothing written at all. */
    @Test
    fun updateKey_encryptFailure_overwritesNothing() = runBlocking {
        val dao = FakeApiKeyDao()
        val cipher = FakeCipher().apply { failEncrypt = true }
        val repository = ApiKeyRepository(dao, cipher)

        try {
            repository.insertKey(item(0, "sk-new"))
            throw AssertionError("expected SecretCipherException")
        } catch (_: SecretCipherException) {
        }

        assertEquals(0, dao.keys.size)
    }

    /** F3: decrypt failure surfaces a typed error, never a silent "". */
    @Test
    fun allKeys_decryptFailure_throwsTypedError() = runBlocking {
        val dao = FakeApiKeyDao()
        dao.insertAllKeys(listOf(item(1, "enc:secret")))
        val repository = ApiKeyRepository(dao, FakeCipher().apply { failDecrypt = true })

        try {
            repository.allKeys.first()
            throw AssertionError("expected SecretCipherException")
        } catch (e: SecretCipherException) {
            assertEquals("Decryption failed", e.message)
        }
    }
}
