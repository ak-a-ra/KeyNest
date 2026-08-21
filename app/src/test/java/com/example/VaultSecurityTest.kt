package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.ApiKeyItem
import com.example.core.security.VaultSecurity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VaultSecurityTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        VaultSecurity.removeMasterPin(context)
    }

    @Test
    fun `test PIN lifecycle - set verify and remove`() {
        assertFalse("PIN should not be set initially", VaultSecurity.isPinSet(context))
        assertFalse("Verification should fail when no PIN set", VaultSecurity.verifyPin(context, "1234"))

        VaultSecurity.setMasterPin(context, "1234")
        assertTrue("PIN should be set", VaultSecurity.isPinSet(context))
        assertTrue("Verification should succeed with correct PIN", VaultSecurity.verifyPin(context, "1234"))
        assertFalse("Verification should fail with incorrect PIN", VaultSecurity.verifyPin(context, "9999"))

        VaultSecurity.removeMasterPin(context)
        assertFalse("PIN should not be set after removal", VaultSecurity.isPinSet(context))
        assertFalse("Verification should fail after removal", VaultSecurity.verifyPin(context, "1234"))
    }

    @Test
    fun `test key masking logic`() {
        // Short keys should be completely masked
        assertEquals("••••••••", VaultSecurity.maskKey("12345", 4))
        assertEquals("••••••••", VaultSecurity.maskKey("", 4))

        // Normal keys should show visibleChars at start and end
        val masked = VaultSecurity.maskKey("sk-proj-1234567890abcdef", 4)
        assertTrue("Should start with prefix", masked.startsWith("sk-p"))
        assertTrue("Should end with suffix", masked.endsWith("cdef"))
        assertTrue("Should contain bullet characters", masked.contains("•"))
    }

    @Test
    fun `test provider detection patterns`() {
        assertEquals("OpenAI", VaultSecurity.detectProviderFromKey("sk-proj-999888777"))
        assertEquals("OpenAI", VaultSecurity.detectProviderFromKey("sk-regular-openai-key"))
        assertEquals("Anthropic Claude", VaultSecurity.detectProviderFromKey("sk-ant-api03-12345"))
        assertEquals("OpenRouter", VaultSecurity.detectProviderFromKey("sk-or-v1-abcde"))
        assertEquals("Google Gemini", VaultSecurity.detectProviderFromKey("AIzaSyDummyGeminiKey"))
        assertEquals("Groq", VaultSecurity.detectProviderFromKey("gsk_GroqSecretKey123"))
        assertEquals("GitHub", VaultSecurity.detectProviderFromKey("ghp_GitHubPersonalAccessToken"))
        assertEquals("GitHub", VaultSecurity.detectProviderFromKey("github_pat_Token123456789"))
        assertEquals("Stripe", VaultSecurity.detectProviderFromKey("sk_live_StripeSecretKey"))
        assertEquals("Stripe", VaultSecurity.detectProviderFromKey("sk_test_StripeTestKey"))
        assertEquals("AWS", VaultSecurity.detectProviderFromKey("AKIAIOSFODNN7EXAMPLE"))
        assertEquals("Hugging Face", VaultSecurity.detectProviderFromKey("hf_HuggingFaceToken"))
        assertEquals("Resend", VaultSecurity.detectProviderFromKey("re_ResendApiKey"))
        assertEquals("Pinecone", VaultSecurity.detectProviderFromKey("pcsk_PineconeApiKey"))
        assertEquals("Custom / Other", VaultSecurity.detectProviderFromKey("random-unrecognized-key-token"))
    }

    @Test
    fun `test entropy calculations`() {
        val emptyEntropy = VaultSecurity.calculateEntropy("")
        assertEquals(0.0, emptyEntropy.entropyBits, 0.01)
        assertEquals("Empty", emptyEntropy.strength)
        assertEquals(0.0f, emptyEntropy.strengthPercent, 0.01f)

        val weakEntropy = VaultSecurity.calculateEntropy("12345")
        assertTrue("Weak entropy bits should be < 35", weakEntropy.entropyBits < 35)
        assertEquals("Weak", weakEntropy.strength)

        val strongEntropy = VaultSecurity.calculateEntropy("k#9P\$mZ!2Q@vL8&wA^7dF%1x")
        assertTrue("Strong entropy bits should be > 60", strongEntropy.entropyBits > 60)
        assertTrue(strongEntropy.strengthPercent > 0.6f)
    }

    @Test
    fun `test custom key generator`() {
        val generated = VaultSecurity.generateCustomKey(
            length = 32,
            useUpper = true,
            useLower = true,
            useNumbers = true,
            useSymbols = false,
            prefix = "test_"
        )
        assertEquals(37, generated.length) // 5 prefix + 32 chars
        assertTrue(generated.startsWith("test_"))

        val uuid = VaultSecurity.generateUuid()
        assertEquals(36, uuid.length)
        assertTrue(uuid.contains("-"))

        val hex = VaultSecurity.generateHex(16)
        assertEquals(32, hex.length) // 16 bytes = 32 hex chars
    }

    @Test
    fun `test theme mode storage`() {
        VaultSecurity.setThemeMode(context, "DARK")
        assertEquals("DARK", VaultSecurity.getThemeMode(context))

        VaultSecurity.setThemeMode(context, "CYBER")
        assertEquals("CYBER", VaultSecurity.getThemeMode(context))
    }
}
