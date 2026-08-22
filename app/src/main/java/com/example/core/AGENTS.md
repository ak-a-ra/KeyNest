# AGENTS.md — Core Subsystems

## Scope

This contract governs `com.example.core.*`:
- `core/database`: Room database (`AppDatabase`), schema migrations, and `ApiKeyDao`.
- `core/security`: Android Keystore AES-256-GCM cryptography (`Cryptography`), `EncryptedSharedPreferences` / fallback security (`VaultSecurity`), PBKDF2/GCM portable backup encryption (`VaultBackupCrypto`).
- `core/model`: Domain entity definitions (`ApiKeyItem`), provider presets (`ProviderPreset`).
- `core/repository`: Persistence boundary (`ApiKeyRepository`) with atomic encryption/decryption.
- `core/files`: Storage Access Framework (SAF) and file I/O operations (`VaultFileManager`).
- `core/designsystem`: Color schemes, typography, and Material 3 theme definitions.
- `core/util`: Formatting helpers and string extensions (`ApiKeyFormatting`).

## Invariants & Security Rules

- **Zero Plaintext Logging:** Never print, log, or leak raw API keys or secrets in Logcat, exceptions, or error messages.
- **Hardware Keystore Storage:** Room secret fields (`apiKey`, `secretKey`) MUST always be encrypted via `Cryptography` before persistence.
- **Portable Backups:** Encrypted backups (`.keynest`) MUST use PBKDF2-HMAC-SHA256 (100k rounds) + AES-256-GCM with salt and IV in `VaultBackupCrypto`.
- **Thread Safety & I/O:** All database and disk file operations MUST run on `Dispatchers.IO` via coroutines / Flow.
- **No Direct UI Coupling:** Core files must not depend on feature UI composables or ViewModels.

## Verification

- Run unit tests in `app/src/test/java/com/example/core/` for any changes to repository, crypto, database, or backup logic.
