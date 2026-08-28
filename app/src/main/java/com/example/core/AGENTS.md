# AGENTS.md — Core Subsystems

## Scope

This contract governs `com.example.core.*`:

- `core/database`: Room database (`AppDatabase`), schema migrations, and `ApiKeyDao`.
- `core/security`: Android Keystore AES-256-GCM cryptography (`SecretCipher`/`KeystoreCipher`), `EncryptedSharedPreferences` / fallback security (`VaultSecurity`), PBKDF2/GCM portable backup encryption (`VaultBackupCrypto`).
- `core/model`: Domain entity definitions (`ApiKeyItem`), provider presets (`ProviderPreset`).
- `core/repository`: Persistence boundary (`ApiKeyRepository`) with atomic encryption/decryption.
- `core/files`: Storage Access Framework (SAF) and file I/O operations (`VaultFileManager`).
- `core/designsystem`: Color schemes, typography, and Material 3 theme definitions.
- `core/util`: Formatting helpers and string extensions (`ApiKeyFormatting`).

## Invariants & Security Rules

- **Zero Plaintext Logging:** Never print, log, or leak raw API keys or secrets in Logcat, exceptions, or error messages.
- **Hardware Keystore Storage:** Room secret fields (`apiKey`, `secretKey`) MUST always be encrypted via `SecretCipher` (`KeystoreCipher`) before persistence.
- **Portable Backups:** Encrypted backups (`.keynest`) MUST use PBKDF2-HMAC-SHA256 (100k rounds) + AES-256-GCM with salt and IV in `VaultBackupCrypto`. Restore MUST reject containers whose `version` != `BACKUP_VERSION` ("Unsupported backup version") and reject file-supplied iteration counts below `ITERATIONS` before key derivation (higher counts stay allowed for forward compatibility).
- **Thread Safety & I/O:** All database and disk file operations MUST run on `Dispatchers.IO` via coroutines / Flow.
- **No Direct UI Coupling:** Core files must not depend on feature UI composables or ViewModels.

## Verification

- Run unit tests in `app/src/test/java/com/example/core/` for any changes to repository, crypto, database, or backup logic.

## Child DOX Index

- [`database/AGENTS.md`](database/AGENTS.md) — Room database, migrations, and DAO flows.
- [`security/AGENTS.md`](security/AGENTS.md) — Keystore AES-256-GCM field encryption, EncryptedSharedPreferences, PBKDF2 backup crypto.
- [`model/AGENTS.md`](model/AGENTS.md) — Domain entities (`ApiKeyItem`) and provider presets.
- [`repository/AGENTS.md`](repository/AGENTS.md) — Encryption persistence boundary (`ApiKeyRepository`).
- [`files/AGENTS.md`](files/AGENTS.md) — Storage Access Framework (SAF) and file I/O operations (`VaultFileManager`).
- [`designsystem/AGENTS.md`](designsystem/AGENTS.md) — Material 3 colors, typography, Keep card tints, and core components.

