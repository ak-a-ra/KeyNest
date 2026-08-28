# AGENTS.md — Core Data Models

## Scope

Governs `com.example.core.model`: `ApiKeyItem.kt` entity and `ProviderPreset.kt`.

## Responsibilities

- Domain data models, provider catalog presets, and key status metadata.

## Invariants

- `ApiKeyItem` must remain annotated with `@Entity(tableName = "api_keys")`.
- Keep schema fields synchronized with Room database migrations and test fixtures.
