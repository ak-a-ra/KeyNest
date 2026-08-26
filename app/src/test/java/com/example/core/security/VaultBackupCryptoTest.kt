package com.example.core.security

import com.example.core.model.ApiKeyItem
import org.json.JSONObject
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
            apiKey = "test_sample_openai_key_123",
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
            apiKey = "test_sample_stripe_key_123",
            secretKey = "test_sample_webhook_secret_123",
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
        assertEquals("test_sample_openai_key_123", key1.apiKey)
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
        assertEquals("test_sample_stripe_key_123", key2.apiKey)
        assertEquals("test_sample_webhook_secret_123", key2.secretKey)
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

    @Test
    fun restoreBackup_withLowIterationCount_rejectedBeforeKeyDerivation() {
        val password = "ValidPassword123".toCharArray()
        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, password).getOrThrow()

        val crafted = JSONObject(backupPayload).put("iterations", 1000).toString()
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(crafted, password)

        assertFalse("Low iteration backup must be rejected", restoreResult.isSuccess)
        assertTrue(
            restoreResult.exceptionOrNull() is SecurityException
        )
    }

    @Test
    fun restoreBackup_withHigherIterationCount_stillRestores() {
        val password = "ValidPassword123".toCharArray()
        // Craft a backup encrypted at a higher-than-current iteration count to
        // verify the floor check permits stronger, forward-compatible backups.
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val iv = ByteArray(12).also { java.security.SecureRandom().nextBytes(it) }
        val factory = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = javax.crypto.spec.SecretKeySpec(
            factory.generateSecret(
                javax.crypto.spec.PBEKeySpec(password, salt, 200_000, 256)
            ).encoded,
            "AES"
        )
        val cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, javax.crypto.spec.GCMParameterSpec(128, iv))
        val payloadBytes = cipher.doFinal("[]".toByteArray())

        fun enc(b: ByteArray) = android.util.Base64.encodeToString(b, android.util.Base64.NO_WRAP)
        val crafted = JSONObject().apply {
            put("app", VaultBackupCrypto.APP_IDENTIFIER)
            put("version", VaultBackupCrypto.BACKUP_VERSION)
            put("salt", enc(salt))
            put("iv", enc(iv))
            put("iterations", 200_000)
            put("payload", enc(payloadBytes))
        }.toString()

        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(crafted, password)
        assertTrue("Higher iteration count must still restore", restoreResult.isSuccess)
        assertEquals(0, restoreResult.getOrThrow().size)
    }

    @Test
    fun restoreBackup_withMissingIterationCount_rejected() {
        val password = "ValidPassword123".toCharArray()
        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, password).getOrThrow()

        val crafted = JSONObject(backupPayload).remove("iterations").toString()
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(crafted, password)

        assertFalse("Missing iteration count must be rejected", restoreResult.isSuccess)
        assertTrue(restoreResult.exceptionOrNull() is SecurityException)
    }

    @Test
    fun restoreBackup_withExcessiveIterationCount_rejected() {
        val password = "ValidPassword123".toCharArray()
        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, password).getOrThrow()

        // KDF DoS guard: absurdly high rounds must not be derivable from the file.
        val crafted = JSONObject(backupPayload).put("iterations", Int.MAX_VALUE).toString()
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(crafted, password)

        assertFalse("Over-cap iteration count must be rejected", restoreResult.isSuccess)
        assertTrue(restoreResult.exceptionOrNull() is SecurityException)
    }

    @Test
    fun restoreBackup_withUnsupportedVersion_failsWithClearError() {
        val password = "ValidPassword123".toCharArray()
        val backupPayload = VaultBackupCrypto.createEncryptedBackup(testKeys, password).getOrThrow()

        val future = JSONObject(backupPayload).put("version", 999).toString()
        val restoreResult = VaultBackupCrypto.restoreEncryptedBackup(future, password)

        assertFalse("Unsupported version must fail", restoreResult.isSuccess)
        assertEquals("Unsupported backup version", restoreResult.exceptionOrNull()?.message)
    }
}
