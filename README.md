<div align="center">

<img src="./assets/keynest_icon.png" width="128" height="128" alt="KeyNest Icon" />

# KeyNest

*Ultra-fast, encrypted API key and developer secret vault for Android.*

[![Build Status](https://img.shields.io/github/actions/workflow/status/ak-a-ra/KeyNest/build-release.yml?style=flat-square&label=Build)](https://github.com/ak-a-ra/KeyNest/actions)
[![Release](https://img.shields.io/github/v/release/ak-a-ra/KeyNest?style=flat-square&label=Release)](https://github.com/ak-a-ra/KeyNest/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20%7C%20API%2024%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)

</div>

---

KeyNest is an offline-first secret vault for Android developers. It stores, organizes, searches, and exports API keys, client secrets, and environment tokens — with Android Keystore AES-256-GCM field encryption and zero plaintext logging.

![KeyNest Vault Showcase](./assets/vault_showcase.png)

## Features

- **Hardware-backed encryption** — AES-256-GCM via Android Keystore; values are masked by default and never logged in plaintext.
- **Reactive search & filtering** — zero-latency search across titles, provider presets, tags, and endpoints.
- **Live provider detection** — badges update as you type key prefixes (`sk-`, `AIza`, `gsk_`, `xai-`, `ghp_`, `AKIA`, …).
- **Tags, colors & environments** — categorize credentials with custom tags and contrast-safe pastel cards.
- **Multi-box secrets** — custom secret fields (client secrets, private keys) plus batch key entry.
- **Rotation tracking** — expiration dates with 3-tier color-coded alerts (fresh / approaching / overdue).
- **PIN lock** — master PIN screen with vault protection and a secure degraded state if crypto can't initialize (never falls back to plaintext).
- **Import / export** — `.env` files, or portable PBKDF2 + AES-256-GCM encrypted `.keynest` vault backups.
- **Clipboard protection** — auto-clearing clipboard with Android 13+ `EXTRA_IS_SENSITIVE` flags.
- **Soft-delete trash** — restore or permanently purge deleted items.
- **Random generator** — in-app secret/token generator with entropy strength analysis.

> [!NOTE]
> KeyNest sets `android:allowBackup="false"` so vault data can never leak through cloud backup or ADB extraction.

## Architecture

Single-activity Jetpack Compose app with a `core/` + `feature/` split:

```text
com.example/
├── core/
│   ├── database/       # Room DB schema, entities, DAOs
│   ├── designsystem/   # Material Design 3 theme
│   ├── files/          # Storage Access Framework (SAF) I/O
│   ├── model/          # Domain models & provider presets
│   ├── repository/     # Encryption-aware repository layer
│   └── security/       # Keystore, EncryptedSharedPreferences, PBKDF2 backup crypto
└── feature/
    ├── export/         # .env and .keynest backup export sheets
    ├── keymanagement/  # Add/edit forms & key detail modals
    ├── search/         # Real-time search UI
    ├── settings/       # PIN lock screen & security audit
    └── vault/          # Home screen, tag carousel, trash view
```

| Layer | Tech |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| Persistence | Room (KSP), EncryptedSharedPreferences |
| Crypto | Android Keystore AES-256-GCM, PBKDF2 (100k rounds) |
| Testing | JUnit, Robolectric, Roborazzi screenshot tests |
| Build | Gradle version catalogs, Secrets plugin (`.env` convention) |

## Getting Started

### Requirements

- Android SDK 36, min API 24 (Android 8.0+)
- JDK 17+

### Build & Run

```bash
git clone https://github.com/ak-a-ra/KeyNest.git
cd KeyNest
./gradlew assembleDebug      # debug APK
./gradlew test               # unit tests
```

Debug builds sign with the bundled `debug.keystore`. Release builds read signing config from `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables.

### Configuration

Secrets follow the web `.env` convention via the Gradle Secrets plugin:

```bash
cp .env.example .env         # then fill in your values
```

## Documentation

- [Roadmap](./ROADMAP.md) — milestones and product vision
- [Plan](./PLAN.md) — implementation phases
- [Optimization spec](./OPTIMIZATION_SPEC.md)
- Architecture decision records live under [`docs/adr/`](./docs/adr/)
