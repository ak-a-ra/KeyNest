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

### Phase 3: Advanced Integrations & Scalability (Q4 2026)
- **KSP Optimization & R8 Strict Mode**: Full static code generation for database schemas and dependency injection to drastically reduce startup time.
- **Zero-Knowledge Cloud Sync (Opt-In)**: Bring-Your-Own-Cloud (WebDAV, Google Drive) synchronization where the vault is fully encrypted locally with PBKDF2 before ever leaving the device.
- **Cross-Device Payload Sync (Air-Gapped)**: Generate dense QR codes for high-entropy secret transfer between physical devices without a network intermediary.

### Phase 4: Platform Ecosystem & Scalable Architecture (Faraway Design)
*Evolution into a distributed, zero-knowledge developer secrets platform.*

- **4.1. Multi-Workspace & Blind FTS5 Indexing**:
  - Logical data partitioning (`workspaces` table) separating Personal, Work, and Client environments with isolated KeyStore aliases.
  - Blind FTS5 full-text search indexing on provider names and tags with zero plaintext leakage of cryptographic secrets.
- **4.2. Zero-Knowledge E2EE Sync Protocol & CRDT Engine**:
  - Decentralized delta synchronization via Hybrid Logical Clocks (HLC) and Last-Write-Wins element sets (LWW-Element-Set CRDT).
  - Outbox staging queue (`sync_outbox`) with ChaCha20-Poly1305 encrypted mutation envelopes for self-hosted relay or WebDAV targets.
- **4.3. Android Autofill Framework & Local IPC Daemon**:
  - Android `AutofillService` implementation to securely autofill API keys into terminal emulators (Termux), IDEs, and browser developer consoles.
  - Local Unix Domain Socket daemon for authenticated CLI access (`keynest get <provider> --env=prod`) gated by biometrics.
- **4.4. Kotlin Multiplatform (KMP) Core & Native Hardware Security**:
  - Decouple `core.security` into KMP expect/actual: Android KeyStore + StrongBox, Apple Keychain + Secure Enclave, and Linux Secret Service / Windows DPAPI.
  - Migrate persistence to SQLDelight for unified schema and migration execution across Android, Desktop (Compose Multiplatform), and CLI.
  - FIDO2 / WebAuthn and physical hardware security key (YubiKey NFC/USB-C) challenge-response authentication.
- **4.5. Enterprise Team Vaults & Shamir's Secret Sharing (SSS)**:
  - High-security production key splits requiring M-of-N quorum approvals to reconstruct master API credentials.
  - Cryptographic tamper-evident audit trail logging every secret access, export, and rotation event.

