# AGENTS.md — Core Security Subsystem

## Scope

Governs `com.example.core.security`: `SecretCipher.kt`, `Cryptography.kt`, `VaultSecurity.kt`, and `VaultBackupCrypto.kt`.

## Responsibilities

- KeyStore-backed AES-256-GCM field encryption & decryption (`KeystoreCipher`).
- EncryptedSharedPreferences master key management and fallback storage.
- Portable backup container (`.keynest`) cryptographic operations (PBKDF2 100k rounds + AES-256-GCM).

## Invariants

- Plaintext secrets must NEVER be logged or written unencrypted to disk.
- Restore must enforce `BACKUP_VERSION` match and reject PBKDF2 iterations below safety floor (`ITERATIONS`).
- Failed decryptions must degrade gracefully or raise explicit exceptions without corrupting data.
