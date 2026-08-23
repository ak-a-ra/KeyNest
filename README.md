<div align="center">

<img src="./assets/keynest_icon.png" width="128" height="128" alt="KeyNest Icon" />

# KeyNest

*An encrypted API key vault for Android, built for developers who juggle too many secrets.*

[![Build Status](https://img.shields.io/github/actions/workflow/status/ak-a-ra/KeyNest/build-release.yml?style=flat-square&label=Build)](https://github.com/ak-a-ra/KeyNest/actions)
[![Release](https://img.shields.io/github/v/release/ak-a-ra/KeyNest?style=flat-square&label=Release)](https://github.com/ak-a-ra/KeyNest/releases)
[![Android](https://img.shields.io/badge/Android-8.0%2B%20%7C%20API%2024%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Unlicense-blue?style=flat-square)](./LICENSE)

</div>

---

If you work with LLM APIs, cloud consoles, and CI tokens, you end up with dozens of keys scattered across notes, dotfiles, and chat history. KeyNest keeps them in one offline vault on your phone. Every secret is field-encrypted with Android Keystore before it touches disk, masked until you reveal it, and never logged in plaintext.

![KeyNest Vault Showcase](./assets/vault_showcase.png)

## What it does

- Stores API keys plus optional secret fields (client secrets, private keys), with batch entry for when you migrate a whole provider at once
- Searches everything instantly: titles, providers, tags, endpoints
- Recognizes key prefixes as you type (`sk-`, `AIza`, `gsk_`, `xai-`, `ghp_`, `AKIA`...) and shows the provider badge live
- Tracks expiration dates with three color tiers: fresh, approaching, overdue
- Locks behind a master PIN, and degrades safely if crypto can't initialize rather than falling back to plaintext
- Imports and exports `.env` files, or full portable backups encrypted with PBKDF2 (100k rounds) + AES-256-GCM
- Copies through an auto-clearing clipboard that uses Android 13+ `EXTRA_IS_SENSITIVE`
- Soft-deletes into a trash bin you can restore from or purge
- Generates random tokens in-app, with entropy strength analysis

> [!NOTE]
> KeyNest sets `android:allowBackup="false"` so vault data can't leak through cloud backup or ADB extraction.

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

## Getting started

You need Android SDK 36 and JDK 17+.

```bash
git clone https://github.com/ak-a-ra/KeyNest.git
cd KeyNest
./gradlew assembleDebug      # debug APK
./gradlew test               # unit tests
```

Debug builds sign with the bundled `debug.keystore`. Release builds read signing config from `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables.

Secrets follow the web `.env` convention via the Gradle Secrets plugin:

```bash
cp .env.example .env         # then fill in your values
```

## Documentation

- [Roadmap](./ROADMAP.md) — milestones and product vision
- [Plan](./PLAN.md) — implementation phases
- [Optimization spec](./OPTIMIZATION_SPEC.md)
- Architecture decision records live under [`docs/adr/`](./docs/adr/)
- [License](./LICENSE) — Unlicense (public domain)
