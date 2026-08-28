# AGENTS.md — Feature Export

## Scope

Governs `com.example.feature.export`: `DeveloperCodeExporterScreen.kt`, `DotEnvExportSheet.kt`, and `VaultBackupSheet.kt`.

## Responsibilities

- Multi-language developer snippet exporter (cURL, Python, Shell, Bearer), `.env` parser & exporter, and encrypted `.keynest` backup sheet.

## Invariants

- Backup export/import must gate passphrase derivation through `VaultBackupCrypto` rules.
