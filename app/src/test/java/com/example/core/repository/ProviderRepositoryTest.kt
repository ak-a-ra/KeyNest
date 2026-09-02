package com.example.core.repository

import android.content.Context
import androidx.room.Room
import com.example.core.database.AppDatabase
import com.example.core.model.ProviderKeyItem
import com.example.core.model.ProviderProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ProviderRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ProviderRepository

    @Before
    fun setUp() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ProviderRepository(db.providerDao(), FakeCipher())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveAndRetrieveProvider_roundTripsEncryptedKeys() = runBlocking {
        val profile = ProviderProfile(
            id = "openai",
            category = "AI & LLMs",
            displayName = "OpenAI",
            baseUrl = "https://api.openai.com/v1",
            colorHex = "#10A37F",
            isActive = true,
            activeKeyId = "key-1",
            keys = listOf(
                ProviderKeyItem(
                    id = "key-1",
                    label = "Prod Key",
                    apiKey = "sk-live-1234567890",
                    isPrimary = true
                ),
                ProviderKeyItem(
                    id = "key-2",
                    label = "Staging Key",
                    apiKey = "sk-test-0987654321",
                    isPrimary = false
                )
            )
        )

        repository.saveProvider(profile)

        val retrievedList = repository.allProviders.first()
        assertEquals(1, retrievedList.size)
        val retrieved = retrievedList.first()
        assertEquals("openai", retrieved.id)
        assertEquals("OpenAI", retrieved.displayName)
        assertEquals(2, retrieved.keys.size)
        assertEquals("sk-live-1234567890", retrieved.keys[0].apiKey)
        assertEquals("sk-test-0987654321", retrieved.keys[1].apiKey)
        assertTrue(retrieved.keys[0].isPrimary)
        assertFalse(retrieved.keys[1].isPrimary)
        assertEquals("sk-live-1234567890", retrieved.activeApiKey)
    }

    @Test
    fun setActiveKey_updatesActiveStatusCorrectly() = runBlocking {
        val profile = ProviderProfile(
            id = "anthropic",
            category = "AI & LLMs",
            displayName = "Anthropic Claude",
            baseUrl = "https://api.anthropic.com",
            colorHex = "#D97706",
            isActive = true,
            activeKeyId = "k1",
            keys = listOf(
                ProviderKeyItem(id = "k1", label = "Key 1", apiKey = "sk-ant-1", isPrimary = true),
                ProviderKeyItem(id = "k2", label = "Key 2", apiKey = "sk-ant-2", isPrimary = false)
            )
        )

        repository.saveProvider(profile)
        repository.setActiveKey("anthropic", "k2")

        val retrieved = repository.allProviders.first().first()
        assertEquals("k2", retrieved.activeKeyId)
        assertEquals("sk-ant-2", retrieved.activeApiKey)
    }

    @Test
    fun softDeleteAndRestore_worksCorrectly() = runBlocking {
        val profile = ProviderProfile(
            id = "gemini",
            category = "AI & LLMs",
            displayName = "Google Gemini",
            baseUrl = "https://generativelanguage.googleapis.com",
            colorHex = "#4285F4",
            isActive = true
        )

        repository.saveProvider(profile)
        assertEquals(1, repository.allProviders.first().size)

        repository.softDeleteProvider("gemini")
        assertEquals(0, repository.allProviders.first().size)
        assertEquals(1, repository.trashedProviders.first().size)

        repository.restoreProvider("gemini")
        assertEquals(1, repository.allProviders.first().size)
        assertEquals(0, repository.trashedProviders.first().size)
    }
}
