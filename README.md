<div align="center">

<img src="./assets/keynest_icon.png" width="128" height="128" alt="KeyNest Icon" />

# KeyNest

*Ultra-fast, hardware-encrypted developer secret and API key vault for Android.*

[![Build Status](https://img.shields.io/github/actions/workflow/status/ak-a-ra/KeyNest/build-release.yml?style=flat-square&label=Build)](https://github.com/ak-a-ra/KeyNest/actions)
[![Release](https://img.shields.io/github/v/release/ak-a-ra/KeyNest?style=flat-square&label=Release)](https://github.com/ak-a-ra/KeyNest/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20%7C%20API%2024%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Custom%20(Free%20%2B%20Commercial%20Credit)-informational?style=flat-square)](./LICENSE)

</div>

---

Modern engineering involves juggling dozens of API keys across OpenAI, Anthropic, Gemini, AWS, Stripe, GitHub, and cloud consoles. Storing them in plaintext notes or communication channels is a security risk.

**KeyNest** is an offline-first Android vault built specifically for developer secrets. Every key is encrypted at rest via hardware-backed **Android Keystore (AES-256-GCM)** before touching storage, masked by default, and never logged or leaked.

![KeyNest Vault Showcase](./assets/vault_showcase.png)

## Features

- **Hardware-Backed Cryptography**: Individual secret fields encrypted using Android Keystore AES-256-GCM with authenticated tags.
- **Provider Recognition**: Instant real-time detection of token formats (`sk-`, `AIza`, `gsk_`, `xai-`, `ghp_`, `AKIA`, `glpat-`...).
- **Multi-Field Secret Bundles**: Store primary keys alongside client secrets, webhook signing keys, and private certificates.
- **Zero-Leak Clipboard**: Sensitive tokens copied with Android 13+ `ClipDescription.EXTRA_IS_SENSITIVE` to suppress system clip previews.
- **Expiration Tracking**: Color-coded lifecycles (Active, Expiring Soon, Expired) with instant visual indicators.
- **PIN & Biometric Shield**: Vault access secured behind master PIN verification with memory-sanitized PIN state.
- **Encrypted Portable Backups**: Full vault export and import protected by PBKDF2 key derivation (100,000 rounds) and AES-256-GCM.
- **.env Sync**: Directly parse and export `.env` configuration files for dev workflows.
- **Secure Password & Token Generator**: Generate high-entropy keys and tokens with configurable length and character sets.
- **Soft Delete & Recovery**: Recover deleted secrets from an encrypted trash bin before permanent purge.

> [!IMPORTANT]
> KeyNest enforces `android:allowBackup="false"` and `android:fullBackupContent="false"` in its manifest. Secrets cannot be extracted via ADB backup or third-party cloud synchronization.

## Architecture

KeyNest follows a strict Clean Architecture & MVVM layout built in Jetpack Compose and Kotlin:

```text
com.example/
├── core/
│   ├── database/       # Room DB schema, entities, and DAOs
│   ├── designsystem/   # Material Design 3 theme, Keep tints, components
│   ├── files/          # Storage Access Framework (SAF) I/O
│   ├── model/          # Domain models, provider presets, visual state
│   ├── repository/     # Encryption-aware repository boundary
│   └── security/       # Keystore AES-256-GCM, EncryptedSharedPreferences, PBKDF2
└── feature/
    ├── export/         # .env and encrypted .keynest backup sheets
    ├── keymanagement/  # Add/edit sheets, secret generator, key details
    ├── search/         # Debounced search and provider filter hub
    ├── settings/       # Master PIN authentication & security audit
    └── vault/          # Main feed, provider chips, and trash bin
```

### Technology Stack

| Component | Implementation |
|---|---|
| **Language & UI** | Kotlin 2.1, Jetpack Compose, Material 3, Navigation Compose |
| **Local Storage** | Room Database (KSP) + EncryptedSharedPreferences |
| **Cryptography** | Android Keystore (AES-256-GCM), PBKDF2 (100k rounds) |
| **Quality & Tests** | JUnit 4, Robolectric, Roborazzi screenshot verification |
| **Build System** | Gradle (Kotlin DSL), Version Catalogs (`libs.versions.toml`) |

## Quick Start

### Prerequisites

- Android Studio Ladybug or newer
- Android SDK 36 (minSdk 24, targetSdk 36)
- JDK 17 or JDK 21

### Clone & Build

```bash
git clone https://github.com/ak-a-ra/KeyNest.git
cd KeyNest

# Compile and assemble debug APK
./gradlew assembleDebug

# Run unit and Robolectric test suite
./gradlew testDebugUnitTest
```

### Environment Configuration

KeyNest uses the Secrets Gradle Plugin with root `.env`:

```bash
cp .env.example .env
```

## Security Model

1. **At-Rest Field Encryption**: Secrets are encrypted prior to database insertion; SQLite files never hold raw secret strings.
2. **Master Secret Segregation**: Master encryption keys are generated inside the Android hardware Keystore (StrongBox / TEE where supported).
3. **Clipboard Protection**: Clipboard entries include sensitivity flags preventing OS-level clipboard loggers from capturing secrets.
4. **Memory Hygiene**: Temporary decrypted char arrays and PIN buffers are cleared promptly following authentication and copy actions.

## Documentation

- [Roadmap](./ROADMAP.md) — Product vision and feature milestones
- [Plan](./PLAN.md) — Development phases and technical tasks
- [Architecture Decision Records](./docs/adr/) — Technical design specs
- [Development Log](./LOG.md) — Chronological implementation history
- [License](./LICENSE) — Free for personal use; commercial attribution required
