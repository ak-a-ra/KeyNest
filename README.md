<div align="center">

<img src="./assets/keynest_icon.png" width="128" height="128" alt="KeyNest Icon" />

# KeyNest

*Ultra-fast, encrypted API key and developer secret vault for Android.*

[![Build Status](https://img.shields.io/github/actions/workflow/status/ak-a-ra/KeyNest/build-release.yml?style=flat-square&label=Build)](https://github.com/ak-a-ra/KeyNest/actions)
[![Android](https://img.shields.io/badge/Android-34+-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)

</div>

---

KeyNest is an offline-first secret vault for Android developers. It securely stores, organizes, searches, and exports API keys, client secrets, and environment tokens with Android Keystore AES-256-GCM encryption and zero plaintext logging.

![KeyNest Vault Showcase](./assets/vault_showcase.png)

## Features

- **Hardware-Backed Encryption:** AES-256-GCM encryption via Android Keystore; masked values by default with zero plaintext logs.
- **Reactive Search & Filtering:** Zero-latency search across titles, provider presets, tags, and endpoints.
- **Tag & Color Organization:** Categorize credentials with custom tags and contrast-safe pastel background cards.
- **Multi-Box Secrets & Batching:** Custom secret fields (Client Secrets, Private Keys) and batch key entry support.
- **Environment & Portable Backups:** Import/export `.env` files or PBKDF2 (100k rounds) + AES-256-GCM encrypted `.keynest` vault backups.
- **Clipboard Protection:** Auto-clearing clipboard with Android 13+ `EXTRA_IS_SENSITIVE` flags.
- **Soft-Delete Trash:** Soft-delete bin with 1-tap restore or permanent purge.

> [!NOTE]  
> KeyNest explicitly sets `android:allowBackup="false"` to prevent secret leaks via cloud backup or ADB extraction.

## Architecture

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

## Getting Started

### Requirements
- Android SDK 34+
- JDK 17+

### Build & Run

```bash
git clone https://github.com/ak-a-ra/KeyNest.git
./gradlew assembleDebug
./gradlew test
```
