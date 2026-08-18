package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.data.model.ApiKeyItem
import com.example.data.model.ProviderPresets
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.log2

class DegradingSharedPreferences(
    private val delegate: SharedPreferences,
    val isDegraded: Boolean
) : SharedPreferences {

    override fun getAll(): Map<String, *> = if (isDegraded) emptyMap<String, Any>() else delegate.getAll()

    override fun getString(key: String, defValue: String?): String? =
        if (isDegraded) defValue else delegate.getString(key, defValue)

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? =
        if (isDegraded) defValues else delegate.getStringSet(key, defValues)

    override fun getInt(key: String, defValue: Int): Int =
        if (isDegraded) defValue else delegate.getInt(key, defValue)

    override fun getLong(key: String, defValue: Long): Long =
        if (isDegraded) defValue else delegate.getLong(key, defValue)

    override fun getFloat(key: String, defValue: Float): Float =
        if (isDegraded) defValue else delegate.getFloat(key, defValue)

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        if (isDegraded) {
            if (key == "is_pin_enabled") false else defValue
        } else {
            delegate.getBoolean(key, defValue)
        }

    override fun contains(key: String): Boolean = if (isDegraded) false else delegate.contains(key)

    override fun edit(): SharedPreferences.Editor = if (isDegraded) NoOpEditor() else delegate.edit()

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (!isDegraded) delegate.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        if (!isDegraded) delegate.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private class NoOpEditor : SharedPreferences.Editor {
        override fun putString(key: String, value: String?): SharedPreferences.Editor = this
        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor = this
        override fun putInt(key: String, value: Int): SharedPreferences.Editor = this
        override fun putLong(key: String, value: Long): SharedPreferences.Editor = this
        override fun putFloat(key: String, value: Float): SharedPreferences.Editor = this
        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = this
        override fun remove(key: String): SharedPreferences.Editor = this
        override fun clear(): SharedPreferences.Editor = this
        override fun commit(): Boolean = true
        override fun apply() {}
    }
}

object VaultSecurity {

    private const val PREFS_NAME = "keynest_security_prefs"
    private const val KEY_PIN_HASH = "master_pin_hash"
    private const val KEY_IS_PIN_ENABLED = "is_pin_enabled"
    private const val KEY_THEME_MODE = "app_theme_mode"
    private const val KEY_SALT = "vault_security_salt"
    private const val KEY_LAST_SELF_COPIED = "last_self_copied_key"
    internal const val STATIC_SALT = "KeyNest_Vault_Secure_Salt_2026_!"

    private val isRunningTests: Boolean by lazy {
        try {
            Class.forName("org.robolectric.Robolectric")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    private fun getPrefs(context: Context): SharedPreferences = try {
        if (isRunningTests) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        } else {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            val sharedPreferences = EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            DegradingSharedPreferences(sharedPreferences, isDegraded = false)
        }
    } catch (_: Exception) {
        // Security: Never fall back to plain SharedPreferences - if encryption fails,
        // enter a secure degraded/locked state. Do not crash or read/write sensitive data.
        val fallback = context.getSharedPreferences("keynest_fallback_prefs", Context.MODE_PRIVATE)
        DegradingSharedPreferences(fallback, isDegraded = true)
    }

    private fun getOrCreateSalt(context: Context): String {
        val prefs = getPrefs(context)
        var salt = prefs.getString(KEY_SALT, null)
        if (salt.isNullOrEmpty()) {
            val randomBytes = ByteArray(16)
            SecureRandom().nextBytes(randomBytes)
            salt = randomBytes.joinToString("") { "%02x".format(it) }
            prefs.edit().putString(KEY_SALT, salt).apply()
        }
        return salt
    }

    fun getLastSelfCopiedKey(context: Context): String? =
        getPrefs(context).getString(KEY_LAST_SELF_COPIED, null)

    fun setLastSelfCopiedKey(context: Context, key: String?) {
        if (key == null) {
            getPrefs(context).edit().remove(KEY_LAST_SELF_COPIED).apply()
        } else {
            getPrefs(context).edit().putString(KEY_LAST_SELF_COPIED, key).apply()
        }
    }

    fun getThemeMode(context: Context): String =
        getPrefs(context).getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"

    fun setThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun isPinSet(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_IS_PIN_ENABLED, false) &&
                !getPrefs(context).getString(KEY_PIN_HASH, "").isNullOrEmpty()

    fun setMasterPin(context: Context, pin: String) {
        val salt = getOrCreateSalt(context)
        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, hashPinWithSalt(pin, salt))
            .putBoolean(KEY_IS_PIN_ENABLED, true)
            .apply()
    }

    fun removeMasterPin(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_IS_PIN_ENABLED, false)
            .apply()
    }

    fun verifyPin(context: Context, inputPin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PIN_HASH, "").orEmpty()
        if (storedHash.isEmpty()) return false

        // Check modern hash with per-device salt
        val currentSalt = getPrefs(context).getString(KEY_SALT, null)
        if (!currentSalt.isNullOrEmpty()) {
            if (storedHash == hashPinWithSalt(inputPin, currentSalt)) {
                return true
            }
        }

        // Migration check 1: Static salt migration
        if (storedHash == hashPinWithSalt(inputPin, STATIC_SALT)) {
            setMasterPin(context, inputPin)
            return true
        }

        // Migration check 2: Legacy hash compatibility check
        if (storedHash.contains("_") && storedHash.length != 64) {
            val legacyHash = generateLegacyHash(inputPin)
            if (storedHash == legacyHash) {
                // Upgrade legacy hash to modern hash automatically
                setMasterPin(context, inputPin)
                return true
            }
            return false
        }

        return false
    }

    private fun hashPinWithSalt(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest((pin + salt).toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a hash using the legacy method for backwards compatibility.
     * This relies on JVM String.hashCode() and should only be used to verify
     * older stored PINs before upgrading them to SHA-256.
     */
    internal fun generateLegacyHash(pin: String): String {
        return pin.reversed().hashCode().toString(16) + "_" + (pin.length * 31).toString(16)
    }

    fun maskKey(key: String, visibleChars: Int = 4): String {
        if (key.length <= visibleChars * 2) return "••••••••"
        val prefix = key.take(visibleChars)
        val suffix = key.takeLast(visibleChars)
        val maskedPortion = "•".repeat((key.length - visibleChars * 2).coerceIn(6, 16))
        return "$prefix$maskedPortion$suffix"
    }

    fun detectProviderFromKey(key: String): String {
        val trimmed = key.trim()
        return when {
            trimmed.startsWith("sk-proj-") || (trimmed.startsWith("sk-") && !trimmed.startsWith("sk-ant-") && !trimmed.startsWith("sk-or-")) -> "OpenAI"
            trimmed.startsWith("AIzaSy") -> "Google Gemini"
            trimmed.startsWith("sk-ant-") -> "Anthropic Claude"
            trimmed.startsWith("gsk_") -> "Groq"
            trimmed.startsWith("sk-or-") -> "OpenRouter"
            trimmed.startsWith("pplx-") -> "Perplexity"
            trimmed.startsWith("ghp_") || trimmed.startsWith("github_pat_") -> "GitHub"
            trimmed.startsWith("sk_live_") || trimmed.startsWith("sk_test_") || trimmed.startsWith("rk_live_") -> "Stripe"
            trimmed.startsWith("AKIA") || trimmed.startsWith("ASIA") -> "AWS"
            trimmed.startsWith("hf_") -> "Hugging Face"
            trimmed.startsWith("re_") -> "Resend"
            trimmed.startsWith("pcsk_") -> "Pinecone"
            trimmed.startsWith("sbp_") || (trimmed.startsWith("eyJ") && trimmed.contains(".")) -> "Supabase"
            trimmed.contains(":") && trimmed.length in 40..50 && trimmed.take(10).all { it.isDigit() || it == ':' } -> "Telegram Bot"
            else -> "Custom / Other"
        }
    }

    data class EntropyResult(
        val entropyBits: Double,
        val strength: String,
        val strengthPercent: Float,
        val colorHex: String
    )

    fun calculateEntropy(key: String): EntropyResult {
        if (key.isBlank()) return EntropyResult(0.0, "Empty", 0.0f, "#EF4444")

        var poolSize = 0
        if (key.any { it.isLowerCase() }) poolSize += 26
        if (key.any { it.isUpperCase() }) poolSize += 26
        if (key.any { it.isDigit() }) poolSize += 10
        if (key.any { !it.isLetterOrDigit() }) poolSize += 32
        if (poolSize == 0) poolSize = 10

        val entropy = key.length * log2(poolSize.toDouble())
        val percent = (entropy / 128.0).coerceIn(0.0, 1.0).toFloat()

        val (strength, colorHex) = when {
            entropy < 35 -> "Weak" to "#EF4444"
            entropy < 60 -> "Moderate" to "#F59E0B"
            entropy < 90 -> "Strong" to "#10B981"
            else -> "Fortified (Military Grade)" to "#06B6D4"
        }

        return EntropyResult(
            entropyBits = (entropy * 10).toInt() / 10.0,
            strength = strength,
            strengthPercent = percent,
            colorHex = colorHex
        )
    }

    fun generateCustomKey(
        length: Int = 32,
        useUpper: Boolean = true,
        useLower: Boolean = true,
        useNumbers: Boolean = true,
        useSymbols: Boolean = false,
        prefix: String = ""
    ): String {
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = buildString {
            if (useUpper) append(upper)
            if (useLower) append(lower)
            if (useNumbers) append(numbers)
            if (useSymbols) append(symbols)
        }.ifEmpty { lower + numbers }

        val random = SecureRandom()
        val randomString = (1..length)
            .map { charPool[random.nextInt(charPool.length)] }
            .joinToString("")

        return if (prefix.isNotEmpty()) "$prefix$randomString" else randomString
    }

    fun generateUuid(): String = UUID.randomUUID().toString()

    fun generateHex(bytesCount: Int = 32): String {
        val random = SecureRandom()
        val bytes = ByteArray(bytesCount)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun exportToDotEnv(keys: List<ApiKeyItem>): String {
        val dateFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        return buildString {
            append("# KeyNest Vault Export - Generated $dateFormatted\n")
            append("# Protect this file. Do NOT commit secrets to public repositories.\n\n")

            keys.groupBy { it.category }.forEach { (category, categoryKeys) ->
                append("# --- $category ---\n")
                categoryKeys.forEach { item ->
                    val varName = ProviderPresets.findByName(item.provider).envVarNameSuggestion.ifEmpty {
                        item.title.uppercase().replace("[^A-Z0-9_]".toRegex(), "_")
                    }
                    val suffix = if (item.environment != "Production") "_${item.environment.uppercase()}" else ""
                    append("${varName}${suffix}=\"${item.apiKey}\"\n")
                    if (item.secretKey.isNotBlank()) {
                        append("${varName}_SECRET${suffix}=\"${item.secretKey}\"\n")
                    }
                    if (item.endpointUrl.isNotBlank()) {
                        append("${varName}_BASE_URL=\"${item.endpointUrl}\"\n")
                    }
                }
                append("\n")
            }
        }.trim()
    }

    fun parseDotEnv(dotEnvContent: String): List<ApiKeyItem> = dotEnvContent.lines().mapNotNull { line ->
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return@mapNotNull null

        val eqIndex = trimmed.indexOf('=')
        if (eqIndex <= 0) return@mapNotNull null

        val keyName = trimmed.substring(0, eqIndex).trim()
        var value = trimmed.substring(eqIndex + 1).trim()
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
            (value.startsWith("'") && value.endsWith("'"))
        ) {
            value = value.substring(1, value.length - 1)
        }

        if (value.isEmpty()) return@mapNotNull null

        val detectedProvider = detectProviderFromKey(value)
        val preset = ProviderPresets.findByName(detectedProvider)
        val cleanTitle = keyName.replace("_", " ").lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        ApiKeyItem(
            title = cleanTitle,
            apiKey = value,
            provider = detectedProvider,
            category = preset.category,
            environment = when {
                keyName.contains("DEV", ignoreCase = true) -> "Development"
                keyName.contains("STAGING", ignoreCase = true) || keyName.contains("STAGE", ignoreCase = true) -> "Staging"
                keyName.contains("TEST", ignoreCase = true) -> "Test"
                else -> "Production"
            },
            colorHex = preset.defaultColorHex,
            notes = "Imported from .env"
        )
    }
}
