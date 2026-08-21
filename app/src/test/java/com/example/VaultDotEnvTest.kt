package com.example

import com.example.core.model.ApiKeyItem
import com.example.core.security.VaultSecurity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultDotEnvTest {

    @Test
    fun `test dotEnv export and parse roundtrip`() {
        val sampleKeys = listOf(
            ApiKeyItem(
                id = 1,
                title = "OpenAI Prod Key",
                apiKey = "sk-proj-sample123456",
                provider = "OpenAI",
                category = "AI & LLMs",
                environment = "Production"
            ),
            ApiKeyItem(
                id = 2,
                title = "Stripe Staging",
                apiKey = "sk_test_stripe987654",
                secretKey = "rk_test_stripe_secret",
                provider = "Stripe",
                category = "Payments",
                environment = "Staging"
            )
        )

        val exported = VaultSecurity.exportToDotEnv(sampleKeys)
        assertTrue("Export should contain OPENAI_API_KEY", exported.contains("OPENAI_API_KEY=\"sk-proj-sample123456\""))
        assertTrue("Export should contain STRIPE_SECRET_KEY_STAGING", exported.contains("STRIPE_SECRET_KEY_STAGING=\"sk_test_stripe987654\""))
        assertTrue("Export should contain STRIPE_SECRET_KEY_SECRET_STAGING", exported.contains("STRIPE_SECRET_KEY_SECRET_STAGING=\"rk_test_stripe_secret\""))

        val parsed = VaultSecurity.parseDotEnv(exported)
        assertTrue("Parsed list should have at least 2 items", parsed.size >= 2)
        val openaiItem = parsed.find { it.apiKey == "sk-proj-sample123456" }
        assertEquals("OpenAI", openaiItem?.provider)
        assertEquals("AI & LLMs", openaiItem?.category)

        val stripeItem = parsed.find { it.apiKey == "sk_test_stripe987654" }
        assertEquals("Stripe", stripeItem?.provider)
        assertEquals("Staging", stripeItem?.environment)
    }

    @Test
    fun `test dotEnv parser handles comments and quotes and environments`() {
        val rawDotEnv = """
            # This is a comment
            
            OPENAI_DEV_KEY="sk-proj-development-key"
            ANTHROPIC_KEY='sk-ant-api03-sample'
            PLAIN_KEY=gsk_plain_groq_key
            # Inactive key
            # AWS_KEY="AKIAIOSFODNN7EXAMPLE"
        """.trimIndent()

        val parsed = VaultSecurity.parseDotEnv(rawDotEnv)
        assertEquals(3, parsed.size)

        assertEquals("Development", parsed[0].environment)
        assertEquals("sk-proj-development-key", parsed[0].apiKey)
        assertEquals("OpenAI", parsed[0].provider)

        assertEquals("sk-ant-api03-sample", parsed[1].apiKey)
        assertEquals("Anthropic Claude", parsed[1].provider)

        assertEquals("gsk_plain_groq_key", parsed[2].apiKey)
        assertEquals("Groq", parsed[2].provider)
    }

    @Test
    fun `test dotEnv parser auto-corrects messy exports, keywords, comments, colons, and companion secrets`() {
        val messyInput = """
            # Messy Docker and Shell export syntax
            export NEXT_PUBLIC_OPENAI_API_KEY="sk-proj-sample-openai-key" # inline comment
            export const ANTHROPIC_DEV_KEY='sk-ant-api03-sample-claude';
            STRIPE_TEST_KEY: "sk_test_stripe_secret_12345",
            AWS_ACCESS_KEY_ID = AKIAIOSFODNN7EXAMPLE
            AWS_SECRET_ACCESS_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
            AWS_ENDPOINT_URL = "https://s3.us-east-1.amazonaws.com"
        """.trimIndent()

        val parsed = VaultSecurity.parseDotEnv(messyInput)
        
        // OpenAI Key
        val openai = parsed.find { it.provider == "OpenAI" }
        assertEquals("sk-proj-sample-openai-key", openai?.apiKey)
        assertEquals("AI & LLMs", openai?.category)

        // Anthropic Claude
        val claude = parsed.find { it.provider == "Anthropic Claude" }
        assertEquals("sk-ant-api03-sample-claude", claude?.apiKey)
        assertEquals("Development", claude?.environment)

        // Stripe
        val stripe = parsed.find { it.provider == "Stripe" }
        assertEquals("sk_test_stripe_secret_12345", stripe?.apiKey)
        assertEquals("Test", stripe?.environment)

        // AWS with companion secret and endpoint auto-pairing
        val aws = parsed.find { it.provider == "AWS" }
        assertEquals("AKIAIOSFODNN7EXAMPLE", aws?.apiKey)
        assertEquals("wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY", aws?.secretKey)
        assertEquals("https://s3.us-east-1.amazonaws.com", aws?.endpointUrl)
    }
}
