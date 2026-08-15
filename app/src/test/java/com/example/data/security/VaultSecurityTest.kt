package com.example.data.security

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class VaultSecurityTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Clear prefs before each test
        context.getSharedPreferences("keynest_security_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun verifyPin_withNewPin_savesAndVerifiesCorrectly() {
        val pin = "1234"
        VaultSecurity.setMasterPin(context, pin)

        assertTrue("Should verify correct PIN", VaultSecurity.verifyPin(context, "1234"))
        assertFalse("Should reject incorrect PIN", VaultSecurity.verifyPin(context, "4321"))
    }

    @Test
    fun verifyPin_withLegacyHash_verifiesAndUpgrades() {
        val pin = "5678"
        val legacyHash = VaultSecurity.generateLegacyHash(pin)
        
        // Manually inject legacy hash into preferences
        context.getSharedPreferences("keynest_security_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("master_pin_hash", legacyHash)
            .putBoolean("is_pin_enabled", true)
            .commit()

        // Verify that the legacy hash is accepted
        assertTrue("Should accept legacy hash", VaultSecurity.verifyPin(context, pin))

        // Verify that it upgraded to SHA-256 hash
        val currentHash = context.getSharedPreferences("keynest_security_prefs", Context.MODE_PRIVATE)
            .getString("master_pin_hash", "")
            
        assertTrue("Hash should have been upgraded", currentHash != legacyHash)
        assertTrue("Upgraded hash should be valid SHA-256 length", currentHash?.length == 64)
    }
    
    @Test
    fun verifyPin_withStaticSaltHash_verifiesAndMigratesToPerDeviceSalt() {
        val pin = "9999"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest((pin + VaultSecurity.STATIC_SALT).toByteArray(Charsets.UTF_8))
        val staticSaltHash = hashBytes.joinToString("") { "%02x".format(it) }

        // Inject static salt hash into preferences with no per-device salt
        context.getSharedPreferences("keynest_security_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("master_pin_hash", staticSaltHash)
            .putBoolean("is_pin_enabled", true)
            .remove("vault_security_salt")
            .commit()

        // Verify that static salt hash is accepted and migrated
        assertTrue("Should accept static salt hash", VaultSecurity.verifyPin(context, pin))

        // Check that per-device salt was generated
        val generatedSalt = context.getSharedPreferences("keynest_security_prefs", Context.MODE_PRIVATE)
            .getString("vault_security_salt", null)
        assertTrue("Per-device salt should be generated", !generatedSalt.isNullOrEmpty())
    }

    @Test
    fun lastSelfCopiedKey_persistsAndClearsCorrectly() {
        val keyText = "sk-proj-test-12345"
        VaultSecurity.setLastSelfCopiedKey(context, keyText)

        org.junit.Assert.assertEquals("Should retrieve stored self copied key", keyText, VaultSecurity.getLastSelfCopiedKey(context))

        VaultSecurity.setLastSelfCopiedKey(context, null)
        org.junit.Assert.assertNull("Should be null after clearing", VaultSecurity.getLastSelfCopiedKey(context))
    }

    @Test
    fun verifyPin_withEmptyHash_returnsFalse() {
        assertFalse("Should return false when no PIN is set", VaultSecurity.verifyPin(context, "1234"))
    }
}
