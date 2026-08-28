# AGENTS.md — Feature Export

## Scope

Governs `com.example.feature.export`: `DotEnvExportSheet.kt` and `VaultBackupSheet.kt`.

## Responsibilities

- `.env` parser & exporter and encrypted `.keynest` backup sheet.

## Invariants

- Backup export/import must gate passphrase derivation through `VaultBackupCrypto` rules.
