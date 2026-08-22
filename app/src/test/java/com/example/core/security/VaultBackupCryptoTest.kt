package com.example.core.security

import com.example.core.model.ApiKeyItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VaultBackupCryptoTest {

    private val testKeys = listOf(
        ApiKeyItem(
            id = 1,
            title = "OpenAI Production",
            apiKey = "sk-proj-test123456789",
            secretKey = "org-sec-987654321",
            provider = "OpenAI",
            category = "AI & LLMs",
            environment = "Production",
            endpointUrl = "https://api.openai.com/v1",
            organizationId = "org-custom-123",
            modelOrProject = "gpt-4o",
            notes = "Main backend AI key",
            tags = "prod,ai,backend",
            isPinned = true,
            createdAt = 1700000000000L,
            expiresAt = 1750000000000L,
            rotationDays = 90,
            colorHex = "#10A37F"
        ),
        ApiKeyItem(
            id = 2,
            title = "Stripe Gateway",
            apiKey = "rk_live_abc123def456",
            secretKey = "whsec_hook123",
            provider = "Stripe",
            category = "Payments",
            environment = "Production",
            endpointUrl = "",
            organizationId = "",
            modelOrProject = "",
            notes = "Billing integration",
            tags = "payments,stripe",
            isPinned = false,
            createdAt = 1710000000000L,
            expiresAt = null,
            rotationDays = null,
            colorHex = "#635BFF"
        )
    )

    @Test
    fun createAndRestoreBackup_withCorrectPassword_restoresAllKeysPrecisely() {
        val password = "SuperSecurePassword123!".toCharArray()

        // 1. Create encrypted backup
        val backupResult = VaultBackupCrypto.createEncryptedBackup(testKeys, password)
        assertTrue("Backup creation should succeed", backupResult.isSuccess)
        val backupPayload = backupResult.getOrThrow()

        // 2. Peek metadata
        val metadataResult = VaultBackupCrypto.peekBackupMetadata(backupPayload)
        assertTrue("Peek metadata should succeed", metadataResult.isSuccess)
        val metadata = metadataResult.getOrThrow()
        assertEquals(VaultBackupCrypto.APP_IDENTIFIER, metadata.app)
        assertEquals(2, metadata.itemCount)
        assertEquals(VaultBackupCrypto.BACKUP_VERSION, metadata.version)

        // 3. Restore backup
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(backupPayload, password)
        assertTrue("Restore should succeed with correct password", restoreResult.isSuccess)
        val restoredKeys = restoreResult.getOrThrow()

        assertEquals(2, restoredKeys.size)
        val key1 = restoredKeys[0]
        assertEquals("OpenAI Production", key1.title)
        assertEquals("sk-proj-test123456789", key1.apiKey)
        assertEquals("org-sec-987654321", key1.secretKey)
        assertEquals("OpenAI", key1.provider)
        assertEquals("AI & LLMs", key1.category)
        assertEquals("Production", key1.environment)
        assertEquals("https://api.openai.com/v1", key1.endpointUrl)
        assertEquals("org-custom-123", key1.organizationId)
        assertEquals("gpt-4o", key1.modelOrProject)
        assertEquals("Main backend AI key", key1.notes)
        assertEquals("prod,ai,backend", key1.tags)
        assertTrue(key1.isPinned)
        assertEquals(1750000000000L, key1.expiresAt)
        assertEquals(90, key1.rotationDays)

        val key2 = restoredKeys[1]
        assertEquals("Stripe Gateway", key2.title)
        assertEquals("rk_live_abc123def456", key2.apiKey)
        assertEquals("whsec_hook123", key2.secretKey)
    }

    @Test
    fun restoreBackup_withWrongPassword_failsSecurely() {
        val correctPassword = "CorrectPassword123".toCharArray()
        val wrongPassword = "WrongPassword999".toCharArray()

        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, correctPassword).getOrThrow()
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(backupPayload, wrongPassword)

        assertFalse("Restore should fail with incorrect password", restoreResult.isSuccess)
        assertNotNull("Should contain security exception", restoreResult.exceptionOrNull())
    }

    @Test
    fun restoreBackup_withTamperedPayload_failsGcmAuthentication() {
        val password = "ValidPassword123".toCharArray()
        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, password).getOrThrow()

        // Tamper with payload by corrupting characters
        val tamperedPayload = backupPayload.replace("payload\": \"", "payload\": \"AA==")

        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(tamperedPayload, password)
        assertFalse("Tampered backup should fail authentication", restoreResult.isSuccess)
    }

    @Test
    fun createBackup_withEmptyPassword_fails() {
        val result = VaultBackupCrypto.createEncryptedBackup(testKeys, charArrayOf())
        assertFalse("Empty password should fail", result.isSuccess)
    }
}
