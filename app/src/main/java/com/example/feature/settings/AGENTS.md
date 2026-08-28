# AGENTS.md — Feature Settings

## Scope

Governs `com.example.feature.settings`: `PinLockScreen.kt`, `PinSettingsSheet.kt`, and `SecurityAuditSheet.kt`.

## Responsibilities

- Master PIN authentication, PIN change dialogs, and security audit analysis.

## Invariants

- PIN hashes must be salted and stored in `EncryptedSharedPreferences`.
