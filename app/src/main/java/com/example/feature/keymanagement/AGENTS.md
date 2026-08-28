# AGENTS.md — Feature Key Management

## Scope

Governs `com.example.feature.keymanagement`: `AddEditKeySheet.kt`, `AddKeyFormSections.kt`, `DropdownSelectorField.kt`, `KeyDetailSheet.kt`, `KeyDetailComponents.kt`, `KeyExpirationStatusCard.kt`, and `KeyGeneratorSheet.kt`.

## Responsibilities

- Single & batch key entry sheets, extra secret fields, provider auto-detection, key detail viewer, and entropy key generator.

## Invariants

- Keep sensitive secret fields masked by default using `PasswordVisualTransformation`.
- All interactive touch targets must meet the 48dp minimum size requirement.
