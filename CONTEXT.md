# KeyNest Context

## Purpose

KeyNest is a single-activity Android vault for developers to store, organize, reveal, copy, import, and export API keys and related secrets.

## Primary domain terms

- **Vault item**: An `ApiKeyItem` holding an API key, optional secret key, and metadata such as provider, category, environment, labels, and rotation details.
- **Secret**: An API key or secret key. It must never be logged or persist unencrypted.
- **Field encryption**: Keystore-backed AES-GCM encryption of `ApiKeyItem.apiKey` and `ApiKeyItem.secretKey` before Room persistence.
- **Sensitive preferences**: PIN state, PIN hash, salt, clipboard self-copy marker, and other protected settings stored through `EncryptedSharedPreferences`.
- **Secure degraded state**: The locked or unavailable state used when Android Keystore or encrypted preferences cannot initialize. It must not fall back to plaintext or crash startup.
- **Legacy vault data**: Data written before field encryption or earlier encryption formats. It requires an explicit migration or recovery path, never implicit plaintext treatment of malformed ciphertext.

## Architecture

- `core/database`: Room schema, entity configuration, and `ApiKeyDao`.
- `core/model`: Persistent domain models (`ApiKeyItem`) and provider presets (`ProviderPreset`).
- `core/repository`: Encryption-aware persistence boundary (`ApiKeyRepository`) for vault items.
- `core/security`: Android Keystore AES-256-GCM cryptography (`Cryptography`), encrypted preferences/fallback (`VaultSecurity`), and portable PBKDF2/GCM backup crypto (`VaultBackupCrypto`).
- `core/files`: Storage Access Framework (SAF) and file I/O operations (`VaultFileManager`).
- `core/designsystem`: Material Design 3 tokens, colors, and typography.
- `feature/*`: Presentation layer (`vault`, `keymanagement`, `search`, `export`, `settings`), ViewModels, and adaptive layouts.
- **DOX Hierarchy**: Structured `AGENTS.md` contracts at root, `app/`, `core/`, `feature/`, and `docs/`.


## Invariants

- Room secrets use Keystore-backed field encryption.
- Sensitive preferences use `EncryptedSharedPreferences`.
- Encryption failures are explicit and atomic. A failure must not be represented by an empty secret or overwrite data.
- Vault data must remain recoverable across supported encryption changes.
- Sensitive text is masked by default and clipboard secrets use `EXTRA_IS_SENSITIVE` on API 33+.
- `android:allowBackup` remains `false`.

## Consumer rules

Read this file before architecture, diagnosis, or TDD work. Read applicable records under `docs/adr/` before changing a decision they cover. Update this file when the ubiquitous language, module boundaries, or system invariants change.
