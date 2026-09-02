# KeyNest Roadmap 🗺️

KeyNest is an encrypted AI Provider and Developer Secret Vault for Android, built with Kotlin, Jetpack Compose, and Material Design 3.

---

## 🎯 Vision
To provide developers and AI practitioners with an ultra-fast, local-first, encrypted vault specifically designed to manage AI provider endpoints (Gemini, OpenAI, Anthropic, DeepSeek, Groq, Ollama, OpenRouter) and developer tool keys with live connection diagnostics, multi-key profiles, and instant `.env` export.

---

## 🛡️ Security Invariants
- **Hardware-Backed Encryption:** Keys stored strictly using Android KeyStore AES-256-GCM (`SecretCipher`).
- **Zero Plaintext Logs:** Sensitive values never logged or leaked in error dumps.
- **Protected Clipboard:** Sensitive clipboard memory flags (`ClipDescription.EXTRA_IS_SENSITIVE`) with automatic 30-second expiry countdown.
- **Encrypted Local Backups:** Password-derived PBKDF2 (100k rounds) + AES-256-GCM `.keynest` container export.

---

## 📅 Product Milestones

### Phase 1: Agora-Style Provider Architecture (🔄 Active Focus)
*Transforming KeyNest into an Agora-inspired AI Provider hub.*
- **Structured Provider Profiles**: One profile per provider (Google Gemini, OpenAI, Anthropic Claude, DeepSeek, Groq, Ollama, OpenRouter, Custom) with custom base URLs and headers.
- **Multi-Key per Provider**: Support multiple named keys (Prod, Staging, Dev) under a single provider profile with a 1-tap **Active Key** toggle.
- **Live Endpoint Connection Testing**: Direct in-app network ping (`GET /v1/models` or provider equivalent) with latency and HTTP status diagnostics.
- **Developer Key Support**: Dedicated section for non-AI developer keys (GitHub, Stripe, Supabase, AWS, Custom).
- **One-Tap Active `.env` Export**: Export all active provider keys into standard environment variable format.

### Phase 2: Advanced Developer Workflows & Integrations (🚀 Q4 2026)
- **Code Snippet Generator**: 1-tap export of boilerplate request code (cURL, Python `requests`, TypeScript, Kotlin Ktor/Retrofit) configured with active keys and endpoints.
- **Model Catalog Inspector**: When connection succeeds, fetch and display available models for the provider (e.g. `gemini-2.5-pro`, `gpt-4o`, `claude-3-5-sonnet`).
- **Encrypted Backup & Migration**: Seamless cross-device import/export with merge and overwrite conflict resolution.
- **Master PIN & Biometric Lock**: Fast biometric prompt (`BiometricPrompt`) to unlock provider secrets.

### Phase 3: CLI & Companion Tooling (⏳ Future)
- **Local Bridge / ADB Stream**: Secure local pairing to pipe active keys into workstation `.env` files without cloud intermediary.
- **Key Rotation & Expiration Reminders**: Track key age and prompt periodic rotation.
- **Offline Proxy Helper**: Local mock server for testing API schemas offline.
