package com.example.data.repository

import com.example.data.security.Cryptography
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class CryptographyTest {

    @Test
    fun encryptAndDecrypt_preservesText() {
        val original = "super_secret_test_key_123"
        val encrypted = Cryptography.encrypt(original)
        val decrypted = Cryptography.decrypt(encrypted)
        
        assertEquals("Decrypted text must match original", original, decrypted)
    }
}
