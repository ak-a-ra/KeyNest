package com.example.core.security

import android.util.Base64
import com.example.core.model.ApiKeyItem
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultBackupCrypto {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256/AES-256-GCM"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val ITERATIONS = 100_000
    private const val MAX_ITERATIONS = 10_000_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 16
    private const val IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    const val BACKUP_VERSION = 1
    const val APP_IDENTIFIER = "KeyNest"

    data class BackupMetadata(
        val version: Int,
        val app: String,
        val createdAt: Long,
        val itemCount: Int,
        val algorithm: String
    )

    fun createEncryptedBackup(keys: List<ApiKeyItem>, passphrase: CharArray): Result<String> {
        if (passphrase.isEmpty()) {
            return Result.failure(IllegalArgumentException("Passphrase cannot be empty"))
        }
        return try {
            val salt = ByteArray(SALT_LENGTH_BYTES)
            val iv = ByteArray(IV_LENGTH_BYTES)
            val random = SecureRandom()
            random.nextBytes(salt)
            random.nextBytes(iv)

            val keySpec = PBEKeySpec(passphrase, salt, ITERATIONS, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKeyBytes = factory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")

            val jsonArray = JSONArray()
            for (key in keys) {
                val obj = JSONObject().apply {
                    put("title", key.title)
                    put("apiKey", key.apiKey)
                    put("secretKey", key.secretKey)
                    put("provider", key.provider)
                    put("category", key.category)
                    put("environment", key.environment)
                    put("endpointUrl", key.endpointUrl)
                    put("organizationId", key.organizationId)
                    put("modelOrProject", key.modelOrProject)
                    put("notes", key.notes)
                    put("tags", key.tags)
                    put("isPinned", key.isPinned)
                    put("createdAt", key.createdAt)
                    if (key.expiresAt != null) put("expiresAt", key.expiresAt)
                    if (key.rotationDays != null) put("rotationDays", key.rotationDays)
                    put("colorHex", key.colorHex)
                }
                jsonArray.put(obj)
            }
            val plaintextBytes = jsonArray.toString().toByteArray(Charsets.UTF_8)

            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            val encryptedBytes = cipher.doFinal(plaintextBytes)

            val envelope = JSONObject().apply {
                put("app", APP_IDENTIFIER)
                put("version", BACKUP_VERSION)
                put("createdAt", System.currentTimeMillis())
                put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                put("iv", Base64.encodeToString(iv, Base64.NO_WRAP))
                put("iterations", ITERATIONS)
                put("algorithm", ALGORITHM)
                put("itemCount", keys.size)
                put("payload", Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
            }

            Result.success(envelope.toString(2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun peekBackupMetadata(backupContent: String): Result<BackupMetadata> {
        return try {
            val json = JSONObject(backupContent)
            val app = json.optString("app", "")
            if (app != APP_IDENTIFIER) {
                return Result.failure(IllegalArgumentException("File is not a valid KeyNest backup"))
            }
            val version = json.optInt("version", 1)
            val createdAt = json.optLong("createdAt", 0L)
            val itemCount = json.optInt("itemCount", 0)
            val algorithm = json.optString("algorithm", ALGORITHM)
            Result.success(BackupMetadata(version, app, createdAt, itemCount, algorithm))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Failed to read backup header: corrupted format", e))
        }
    }

    fun restoreEncryptedBackup(backupContent: String, passphrase: CharArray): Result<List<ApiKeyItem>> {
        if (passphrase.isEmpty()) {
            return Result.failure(IllegalArgumentException("Passphrase cannot be empty"))
        }
        return try {
            val json = JSONObject(backupContent)
            val app = json.optString("app", "")
            if (app != APP_IDENTIFIER) {
                return Result.failure(IllegalArgumentException("File is not a valid KeyNest backup"))
            }
            val version = json.optInt("version", -1)
            if (version != BACKUP_VERSION) {
                return Result.failure(IllegalArgumentException("Unsupported backup version"))
            }
            val saltBase64 = json.getString("salt")
            val ivBase64 = json.getString("iv")
            val payloadBase64 = json.getString("payload")
            // ponytail: floor+cap on file-supplied iterations — attacker low counts weaken
            // KDF, absurd high counts enable DoS; generous cap keeps future stronger
            // backups forward-compatible.
            val iterations = json.optInt("iterations", -1)
            if (iterations < ITERATIONS || iterations > MAX_ITERATIONS) {
                return Result.failure(SecurityException("Invalid backup parameters"))
            }

            val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val encryptedBytes = Base64.decode(payloadBase64, Base64.NO_WRAP)

            val keySpec = PBEKeySpec(passphrase, salt, iterations, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val secretKeyBytes = factory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")

            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val plaintext = String(decryptedBytes, Charsets.UTF_8)

            val jsonArray = JSONArray(plaintext)
            val list = mutableListOf<ApiKeyItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = ApiKeyItem(
                    id = 0, // Reset id for insertion on new device
                    title = obj.optString("title", "Imported Secret"),
                    apiKey = obj.optString("apiKey", ""),
                    secretKey = obj.optString("secretKey", ""),
                    provider = obj.optString("provider", "Other"),
                    category = obj.optString("category", "AI & LLMs"),
                    environment = obj.optString("environment", "Production"),
                    endpointUrl = obj.optString("endpointUrl", ""),
                    organizationId = obj.optString("organizationId", ""),
                    modelOrProject = obj.optString("modelOrProject", ""),
                    notes = obj.optString("notes", ""),
                    tags = obj.optString("tags", ""),
                    isPinned = obj.optBoolean("isPinned", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    expiresAt = if (obj.has("expiresAt") && !obj.isNull("expiresAt")) obj.optLong("expiresAt") else null,
                    rotationDays = if (obj.has("rotationDays") && !obj.isNull("rotationDays")) obj.optInt("rotationDays") else null,
                    colorHex = obj.optString("colorHex", "#FFB703")
                )
                list.add(item)
            }

            Result.success(list)
        } catch (e: javax.crypto.AEADBadTagException) {
            Result.failure(SecurityException("Incorrect password or corrupted backup file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
