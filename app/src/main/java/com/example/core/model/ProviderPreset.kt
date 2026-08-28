package com.example.core.model

data class ProviderPreset(
    val name: String,
    val category: String,
    val defaultPrefix: String = "",
    val placeholderKey: String,
    val defaultEndpoint: String = "",
    val defaultColorHex: String,
    val consoleUrl: String,
    val patternRegex: String = "",
    val envVarNameSuggestion: String
)

object ProviderPresets {
    val list = listOf(
        ProviderPreset(
            name = "OpenAI",
            category = "AI & LLMs",
            defaultPrefix = "sk-",
            placeholderKey = "sample_openai_key_placeholder...",
            defaultEndpoint = "https://api.openai.com/v1",
            defaultColorHex = "#10A37F",
            consoleUrl = "https://platform.openai.com/api-keys",
            patternRegex = "^sk-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "OPENAI_API_KEY"
        ),
        ProviderPreset(
            name = "Google Gemini",
            category = "AI & LLMs",
            defaultPrefix = "AIzaSy",
            placeholderKey = "sample_gemini_key_placeholder...",
            defaultEndpoint = "https://generativelanguage.googleapis.com/v1beta",
            defaultColorHex = "#4285F4",
            consoleUrl = "https://aistudio.google.com/app/apikey",
            patternRegex = "^AIzaSy[A-Za-z0-9_-]{33}$",
            envVarNameSuggestion = "GEMINI_API_KEY"
        ),
        ProviderPreset(
            name = "Anthropic Claude",
            category = "AI & LLMs",
            defaultPrefix = "sk-ant-",
            placeholderKey = "sk-ant-api03-abc123...",
            defaultEndpoint = "https://api.anthropic.com/v1",
            defaultColorHex = "#D97757",
            consoleUrl = "https://console.anthropic.com/settings/keys",
            patternRegex = "^sk-ant-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "ANTHROPIC_API_KEY"
        ),
        ProviderPreset(
            name = "DeepSeek",
            category = "AI & LLMs",
            defaultPrefix = "sk-",
            placeholderKey = "sk-deepseek-abc123...",
            defaultEndpoint = "https://api.deepseek.com",
            defaultColorHex = "#0284C7",
            consoleUrl = "https://platform.deepseek.com/api_keys",
            patternRegex = "^sk-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "DEEPSEEK_API_KEY"
        ),
        ProviderPreset(
            name = "Groq",
            category = "AI & LLMs",
            defaultPrefix = "gsk_",
            placeholderKey = "gsk_abc123...",
            defaultEndpoint = "https://api.groq.com/openai/v1",
            defaultColorHex = "#F55036",
            consoleUrl = "https://console.groq.com/keys",
            patternRegex = "^gsk_[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "GROQ_API_KEY"
        ),
        ProviderPreset(
            name = "Mistral AI",
            category = "AI & LLMs",
            defaultPrefix = "",
            placeholderKey = "mis_abc123...",
            defaultEndpoint = "https://api.mistral.ai/v1",
            defaultColorHex = "#FF7000",
            consoleUrl = "https://console.mistral.ai/api-keys",
            patternRegex = "",
            envVarNameSuggestion = "MISTRAL_API_KEY"
        ),
        ProviderPreset(
            name = "Perplexity",
            category = "AI & LLMs",
            defaultPrefix = "pplx-",
            placeholderKey = "pplx-abc123...",
            defaultEndpoint = "https://api.perplexity.ai",
            defaultColorHex = "#20B2AA",
            consoleUrl = "https://www.perplexity.ai/settings/api",
            patternRegex = "^pplx-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "PERPLEXITY_API_KEY"
        ),
        ProviderPreset(
            name = "OpenRouter",
            category = "AI & LLMs",
            defaultPrefix = "sk-or-",
            placeholderKey = "sk-or-v1-abc123...",
            defaultEndpoint = "https://openrouter.ai/api/v1",
            defaultColorHex = "#6366F1",
            consoleUrl = "https://openrouter.ai/keys",
            patternRegex = "^sk-or-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "OPENROUTER_API_KEY"
        ),
        ProviderPreset(
            name = "Hugging Face",
            category = "AI & LLMs",
            defaultPrefix = "hf_",
            placeholderKey = "hf_abc123...",
            defaultEndpoint = "https://api-inference.huggingface.co",
            defaultColorHex = "#FFD21E",
            consoleUrl = "https://huggingface.co/settings/tokens",
            patternRegex = "^hf_[A-Za-z0-9]{34,}$",
            envVarNameSuggestion = "HUGGINGFACE_API_KEY"
        ),
        ProviderPreset(
            name = "GitHub",
            category = "Dev Tools",
            defaultPrefix = "ghp_",
            placeholderKey = "sample_github_token_placeholder...",
            defaultEndpoint = "https://api.github.com",
            defaultColorHex = "#E2E8F0",
            consoleUrl = "https://github.com/settings/tokens",
            patternRegex = "^(ghp_|github_pat_)[A-Za-z0-9_]{36,}$",
            envVarNameSuggestion = "GITHUB_TOKEN"
        ),
        ProviderPreset(
            name = "Stripe",
            category = "Payments",
            defaultPrefix = "sk_",
            placeholderKey = "sample_stripe_key_placeholder...",
            defaultEndpoint = "https://api.stripe.com/v1",
            defaultColorHex = "#635BFF",
            consoleUrl = "https://dashboard.stripe.com/apikeys",
            patternRegex = "^sk_(live|test)_[A-Za-z0-9]{24,}$",
            envVarNameSuggestion = "STRIPE_SECRET_KEY"
        ),
        ProviderPreset(
            name = "AWS",
            category = "Cloud & Infra",
            defaultPrefix = "AKIA",
            placeholderKey = "sample_aws_key_placeholder...",
            defaultEndpoint = "https://aws.amazon.com",
            defaultColorHex = "#FF9900",
            consoleUrl = "https://console.aws.amazon.com/iam/home#/security_credentials",
            patternRegex = "^(AKIA|ASIA)[0-9A-Z]{16}$",
            envVarNameSuggestion = "AWS_ACCESS_KEY_ID"
        ),
        ProviderPreset(
            name = "Supabase",
            category = "Auth & DB",
            defaultPrefix = "sbp_",
            placeholderKey = "sample_supabase_jwt_placeholder...",
            defaultEndpoint = "https://<project-id>.supabase.co",
            defaultColorHex = "#3ECF8E",
            consoleUrl = "https://supabase.com/dashboard/project/_/settings/api",
            patternRegex = "",
            envVarNameSuggestion = "SUPABASE_KEY"
        ),
        ProviderPreset(
            name = "Firebase",
            category = "Auth & DB",
            defaultPrefix = "AIzaSy",
            placeholderKey = "sample_firebase_key_placeholder...",
            defaultEndpoint = "https://firebase.google.com",
            defaultColorHex = "#FFCA28",
            consoleUrl = "https://console.firebase.google.com/project/_/settings/general",
            patternRegex = "",
            envVarNameSuggestion = "FIREBASE_API_KEY"
        ),
        ProviderPreset(
            name = "Resend",
            category = "Dev Tools",
            defaultPrefix = "re_",
            placeholderKey = "re_abc123...",
            defaultEndpoint = "https://api.resend.com",
            defaultColorHex = "#14B8A6",
            consoleUrl = "https://resend.com/api-keys",
            patternRegex = "^re_[A-Za-z0-9_]{20,}$",
            envVarNameSuggestion = "RESEND_API_KEY"
        ),
        ProviderPreset(
            name = "Vercel",
            category = "Cloud & Infra",
            defaultPrefix = "",
            placeholderKey = "vc_token_...",
            defaultEndpoint = "https://api.vercel.com",
            defaultColorHex = "#FFFFFF",
            consoleUrl = "https://vercel.com/account/tokens",
            patternRegex = "",
            envVarNameSuggestion = "VERCEL_TOKEN"
        ),
        ProviderPreset(
            name = "ElevenLabs",
            category = "AI & LLMs",
            defaultPrefix = "",
            placeholderKey = "el_abc123...",
            defaultEndpoint = "https://api.elevenlabs.io/v1",
            defaultColorHex = "#A855F7",
            consoleUrl = "https://elevenlabs.io/app/settings/api-keys",
            patternRegex = "",
            envVarNameSuggestion = "ELEVENLABS_API_KEY"
        ),
        ProviderPreset(
            name = "Pinecone",
            category = "AI & LLMs",
            defaultPrefix = "pcsk_",
            placeholderKey = "pcsk_abc123...",
            defaultEndpoint = "https://api.pinecone.io",
            defaultColorHex = "#3B82F6",
            consoleUrl = "https://app.pinecone.io/keys",
            patternRegex = "^pcsk_[A-Za-z0-9_]{20,}$",
            envVarNameSuggestion = "PINECONE_API_KEY"
        ),
        ProviderPreset(
            name = "Discord Bot",
            category = "Dev Tools",
            defaultPrefix = "",
            placeholderKey = "MTE...bot.token.here",
            defaultEndpoint = "https://discord.com/api/v10",
            defaultColorHex = "#5865F2",
            consoleUrl = "https://discord.com/developers/applications",
            patternRegex = "",
            envVarNameSuggestion = "DISCORD_BOT_TOKEN"
        ),
        ProviderPreset(
            name = "Telegram Bot",
            category = "Dev Tools",
            defaultPrefix = "",
            placeholderKey = "123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ",
            defaultEndpoint = "https://api.telegram.org",
            defaultColorHex = "#229ED9",
            consoleUrl = "https://t.me/BotFather",
            patternRegex = "^[0-9]{8,10}:[a-zA-Z0-9_-]{35}$",
            envVarNameSuggestion = "TELEGRAM_BOT_TOKEN"
        ),
        ProviderPreset(
            name = "Custom / Other",
            category = "Other",
            defaultPrefix = "",
            placeholderKey = "Paste custom secret key here...",
            defaultEndpoint = "",
            defaultColorHex = "#FFB703",
            consoleUrl = "",
            patternRegex = "",
            envVarNameSuggestion = "API_KEY"
        )
    )

    fun findByName(name: String): ProviderPreset = list.find { it.name.equals(name, ignoreCase = true) } ?: list.last()
    

    /**
     * Unified single-source-of-truth provider detector for raw keys/tokens.
     */
    fun detectProvider(key: String): String = com.example.core.security.VaultSecurity.detectProviderFromKey(key)
    

    val categories = listOf(
        "All",
        "AI & LLMs",
        "Cloud & Infra",
        "Payments",
        "Auth & DB",
        "Dev Tools",
        "Other"
    )

    val environments = listOf(
        "Production",
        "Staging",
        "Development",
        "Test",
        "Personal"
    )
}
