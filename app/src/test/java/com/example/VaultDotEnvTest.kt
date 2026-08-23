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
                apiKey = "sample_openai_key_123456",
                provider = "OpenAI",
                category = "AI & LLMs",
                environment = "Production"
            ),
            ApiKeyItem(
                id = 2,
                title = "Stripe Staging",
                apiKey = "sample_stripe_key_987654",
                secretKey = "sample_stripe_secret_key",
                provider = "Stripe",
                category = "Payments",
                environment = "Staging"
            )
        )

        val exported = VaultSecurity.exportToDotEnv(sampleKeys)
        assertTrue("Export should contain OPENAI_API_KEY", exported.contains("OPENAI_API_KEY=\"sample_openai_key_123456\""))
        assertTrue("Export should contain STRIPE_SECRET_KEY_STAGING", exported.contains("STRIPE_SECRET_KEY_STAGING=\"sample_stripe_key_987654\""))
        assertTrue("Export should contain STRIPE_SECRET_KEY_SECRET_STAGING", exported.contains("STRIPE_SECRET_KEY_SECRET_STAGING=\"sample_stripe_secret_key\""))

        val parsed = VaultSecurity.parseDotEnv(exported)
        assertTrue("Parsed list should have at least 2 items", parsed.size >= 2)
        val openaiItem = parsed.find { it.apiKey == "sample_openai_key_123456" }
        assertEquals("OpenAI", openaiItem?.provider)
        assertEquals("AI & LLMs", openaiItem?.category)

        val stripeItem = parsed.find { it.apiKey == "sample_stripe_key_987654" }
        assertEquals("Stripe", stripeItem?.provider)
        assertEquals("Staging", stripeItem?.environment)
    }

    @Test
    fun `test dotEnv parser handles comments and quotes and environments`() {
        val rawDotEnv = """
            # This is a comment
            
            OPENAI_DEV_KEY="sample_openai_dev_key"
            ANTHROPIC_KEY='sample_anthropic_dev_key'
            PLAIN_KEY=gsk_plain_groq_key
            # Inactive key
            # AWS_KEY="sample_aws_key_disabled"
        """.trimIndent()

        val parsed = VaultSecurity.parseDotEnv(rawDotEnv)
        assertEquals(3, parsed.size)

        assertEquals("Development", parsed[0].environment)
        assertEquals("sample_openai_dev_key", parsed[0].apiKey)
        assertEquals("OpenAI", parsed[0].provider)

        assertEquals("sample_anthropic_dev_key", parsed[1].apiKey)
        assertEquals("Anthropic Claude", parsed[1].provider)

        assertEquals("gsk_plain_groq_key", parsed[2].apiKey)
        assertEquals("Groq", parsed[2].provider)
    }

    @Test
    fun `test dotEnv parser auto-corrects messy exports, keywords, comments, colons, and companion secrets`() {
        val messyInput = """
            # Messy Docker and Shell export syntax
            export NEXT_PUBLIC_OPENAI_API_KEY="sample_openai_key_messy" # inline comment
            export const ANTHROPIC_DEV_KEY='sample_anthropic_key_messy';
            STRIPE_TEST_KEY: "sample_stripe_key_messy",
            AWS_ACCESS_KEY_ID = sample_aws_access_key_messy
            AWS_SECRET_ACCESS_KEY = "sample_aws_secret_key_messy"
            AWS_ENDPOINT_URL = "https://s3.us-east-1.amazonaws.com"
        """.trimIndent()

        val parsed = VaultSecurity.parseDotEnv(messyInput)
        
        // OpenAI Key
        val openai = parsed.find { it.provider == "OpenAI" }
        assertEquals("sample_openai_key_messy", openai?.apiKey)
        assertEquals("AI & LLMs", openai?.category)

        // Anthropic Claude
        val claude = parsed.find { it.provider == "Anthropic Claude" }
        assertEquals("sample_anthropic_key_messy", claude?.apiKey)
        assertEquals("Development", claude?.environment)

        // Stripe
        val stripe = parsed.find { it.provider == "Stripe" }
        assertEquals("sample_stripe_key_messy", stripe?.apiKey)
        assertEquals("Test", stripe?.environment)

        // AWS with companion secret and endpoint auto-pairing
        val aws = parsed.find { it.provider == "AWS" }
        assertEquals("sample_aws_access_key_messy", aws?.apiKey)
        assertEquals("sample_aws_secret_key_messy", aws?.secretKey)
        assertEquals("https://s3.us-east-1.amazonaws.com", aws?.endpointUrl)
    }
}
