# Domain Documentation Layout

## Layout

This is a single-context Android application.

- Root context: [`CONTEXT.md`](../../CONTEXT.md)
- Architecture decisions: `docs/adr/`
- Agent governance: [`AGENTS.md`](../../AGENTS.md)
- Product overview: [`README.md`](../../README.md)
- Product direction: [`ROADMAP.md`](../../ROADMAP.md)

## Consumer rules

- Read `CONTEXT.md` before `diagnose`, `tdd`, or `improve-codebase-architecture` work.
- Read relevant ADRs before changing a documented architectural decision.
- Add a numbered ADR for durable, consequential decisions that affect architecture, storage, security, or migration behavior.
- Keep `CONTEXT.md` concise and update it when domain language, module boundaries, or durable invariants change.

## Module boundaries

- `app/src/main/java/com/example/core/database/`: Room database and DAO (`AppDatabase`, `ApiKeyDao`).
- `app/src/main/java/com/example/core/model/`: Domain data structures and provider presets (`ApiKeyItem`, `ProviderPreset`).
- `app/src/main/java/com/example/core/repository/`: Encryption-aware vault persistence (`ApiKeyRepository`).
- `app/src/main/java/com/example/core/security/`: Keystore cryptography, encrypted preferences, and backup crypto (`Cryptography`, `VaultSecurity`, `VaultBackupCrypto`).
- `app/src/main/java/com/example/core/files/`: Storage Access Framework and file I/O operations (`VaultFileManager`).
- `app/src/main/java/com/example/core/designsystem/`: Theme, color palettes, and typography.
- `app/src/main/java/com/example/feature/`: Jetpack Compose screens, sheets, ViewModels, and adaptive layouts (`vault/`, `keymanagement/`, `search/`, `export/`, `settings/`).

