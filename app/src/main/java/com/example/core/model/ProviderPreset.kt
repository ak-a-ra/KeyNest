package com.example.core.model

enum class AuthType {
    BEARER,
    QUERY_PARAM,
    HEADER_X_API_KEY,
    HEADER_APIKEY,
    TOKEN,
    NONE
}

data class ProviderPreset(
    val id: String = "",
    val name: String,
    val category: String,
    val defaultPrefix: String = "",
    val placeholderKey: String,
    val defaultEndpoint: String = "",
    val defaultColorHex: String,
    val consoleUrl: String,
    val patternRegex: String = "",
    val envVarNameSuggestion: String,
    val probePath: String = "",
    val authType: AuthType = AuthType.BEARER,
    val defaultHeaders: Map<String, String> = emptyMap()
)

object ProviderPresets {
    val list = listOf(
        ProviderPreset(
            id = "gemini",
            name = "Google Gemini",
            category = "AI & LLMs",
            defaultPrefix = "AIzaSy",
            placeholderKey = "AIzaSyD-sample-gemini-key-placeholder...",
            defaultEndpoint = "https://generativelanguage.googleapis.com",
            defaultColorHex = "#4285F4",
            consoleUrl = "https://aistudio.google.com/app/apikey",
            patternRegex = "^AIzaSy[A-Za-z0-9_-]{33}$",
            envVarNameSuggestion = "GEMINI_API_KEY",
            probePath = "/v1beta/models?key={apiKey}",
            authType = AuthType.QUERY_PARAM
        ),
        ProviderPreset(
            id = "openai",
            name = "OpenAI",
            category = "AI & LLMs",
            defaultPrefix = "sk-",
            placeholderKey = "sk-proj-sample_openai_key_placeholder...",
            defaultEndpoint = "https://api.openai.com/v1",
            defaultColorHex = "#10A37F",
            consoleUrl = "https://platform.openai.com/api-keys",
            patternRegex = "^sk-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "OPENAI_API_KEY",
            probePath = "/models",
            authType = AuthType.BEARER
        ),
        ProviderPreset(
            id = "anthropic",
            name = "Anthropic Claude",
            category = "AI & LLMs",
            defaultPrefix = "sk-ant-",
            placeholderKey = "sk-ant-api03-sample_anthropic_key...",
            defaultEndpoint = "https://api.anthropic.com/v1",
            defaultColorHex = "#D97757",
            consoleUrl = "https://console.anthropic.com/settings/keys",
            patternRegex = "^sk-ant-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "ANTHROPIC_API_KEY",
            probePath = "/models",
            authType = AuthType.HEADER_X_API_KEY,
            defaultHeaders = mapOf("anthropic-version" to "2023-06-01")
        ),
        ProviderPreset(
            id = "deepseek",
            name = "DeepSeek",
            category = "AI & LLMs",
            defaultPrefix = "sk-",
            placeholderKey = "sk-deepseek-sample-key...",
            defaultEndpoint = "https://api.deepseek.com",
            defaultColorHex = "#0284C7",
            consoleUrl = "https://platform.deepseek.com/api_keys",
            patternRegex = "^sk-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "DEEPSEEK_API_KEY",
            probePath = "/models",
            authType = AuthType.BEARER
        ),
        ProviderPreset(
            id = "groq",
            name = "Groq",
            category = "AI & LLMs",
            defaultPrefix = "gsk_",
            placeholderKey = "gsk_sample_groq_key...",
            defaultEndpoint = "https://api.groq.com/openai/v1",
            defaultColorHex = "#F55036",
            consoleUrl = "https://console.groq.com/keys",
            patternRegex = "^gsk_[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "GROQ_API_KEY",
            probePath = "/models",
            authType = AuthType.BEARER
        ),
        ProviderPreset(
            id = "ollama",
            name = "Ollama",
            category = "AI & LLMs",
            defaultPrefix = "",
            placeholderKey = "Optional API key for remote Ollama...",
            defaultEndpoint = "http://localhost:11434",
            defaultColorHex = "#F8FAFC",
            consoleUrl = "https://ollama.com",
            patternRegex = "",
            envVarNameSuggestion = "OLLAMA_HOST",
            probePath = "/api/tags",
            authType = AuthType.NONE
        ),
        ProviderPreset(
            id = "openrouter",
            name = "OpenRouter",
            category = "AI & LLMs",
            defaultPrefix = "sk-or-",
            placeholderKey = "sk-or-v1-sample_openrouter_key...",
            defaultEndpoint = "https://openrouter.ai/api/v1",
            defaultColorHex = "#6366F1",
            consoleUrl = "https://openrouter.ai/keys",
            patternRegex = "^sk-or-[A-Za-z0-9_-]{20,}$",
            envVarNameSuggestion = "OPENROUTER_API_KEY",
            probePath = "/models",
            authType = AuthType.BEARER
        ),
        ProviderPreset(
            id = "github",
            name = "GitHub",
            category = "Dev Tools",
            defaultPrefix = "ghp_",
            placeholderKey = "ghp_sample_github_token...",
            defaultEndpoint = "https://api.github.com",
            defaultColorHex = "#E2E8F0",
            consoleUrl = "https://github.com/settings/tokens",
            patternRegex = "^(ghp_|github_pat_)[A-Za-z0-9_]{36,}$",
            envVarNameSuggestion = "GITHUB_TOKEN",
            probePath = "/user",
            authType = AuthType.TOKEN,
            defaultHeaders = mapOf("User-Agent" to "KeyNest")
        ),
        ProviderPreset(
            id = "stripe",
            name = "Stripe",
            category = "Payments",
            defaultPrefix = "sk_",
            placeholderKey = "sk_live_sample_stripe_key...",
            defaultEndpoint = "https://api.stripe.com/v1",
            defaultColorHex = "#635BFF",
            consoleUrl = "https://dashboard.stripe.com/apikeys",
            patternRegex = "^sk_(live|test)_[A-Za-z0-9]{24,}$",
            envVarNameSuggestion = "STRIPE_SECRET_KEY",
            probePath = "/balance",
            authType = AuthType.BEARER
        ),
        ProviderPreset(
            id = "supabase",
            name = "Supabase",
            category = "Auth & DB",
            defaultPrefix = "sbp_",
            placeholderKey = "eyJhbGciOi...sample_supabase_jwt",
            defaultEndpoint = "https://your-project.supabase.co/rest/v1",
            defaultColorHex = "#3ECF8E",
            consoleUrl = "https://supabase.com/dashboard/project/_/settings/api",
            patternRegex = "",
            envVarNameSuggestion = "SUPABASE_KEY",
            probePath = "/",
            authType = AuthType.HEADER_APIKEY
        ),
        ProviderPreset(
            id = "aws",
            name = "AWS",
            category = "Cloud & Infra",
            defaultPrefix = "AKIA",
            placeholderKey = "AKIAIOSFODNN7EXAMPLE",
            defaultEndpoint = "https://aws.amazon.com",
            defaultColorHex = "#FF9900",
            consoleUrl = "https://console.aws.amazon.com/iam/home#/security_credentials",
            patternRegex = "^(AKIA|ASIA)[0-9A-Z]{16}$",
            envVarNameSuggestion = "AWS_ACCESS_KEY_ID",
            probePath = "",
            authType = AuthType.NONE
        ),
        ProviderPreset(
            id = "custom",
            name = "Custom / Other",
            category = "Other",
            defaultPrefix = "",
            placeholderKey = "Paste custom secret key here...",
            defaultEndpoint = "https://api.example.com",
            defaultColorHex = "#FFB703",
            consoleUrl = "",
            patternRegex = "",
            envVarNameSuggestion = "API_KEY",
            probePath = "",
            authType = AuthType.BEARER
        )
    )

    fun findByName(name: String): ProviderPreset =
        list.find { it.name.equals(name, ignoreCase = true) || it.id.equals(name, ignoreCase = true) } ?: list.last()

    fun findById(id: String): ProviderPreset =
        list.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: list.last()

    /**
     * Unified single-source-of-truth provider detector for raw keys/tokens.
     */
    fun detectProvider(key: String): String = com.example.core.security.VaultSecurity.detectProviderFromKey(key)

    val categories = listOf(
        "All",
        "AI & LLMs",
        "Dev Tools",
        "Payments",
        "Auth & DB",
        "Cloud & Infra",
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
