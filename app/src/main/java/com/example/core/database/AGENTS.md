# AGENTS.md — Core Database Subsystem

## Scope

Governs `com.example.core.database`: `AppDatabase.kt`, `ApiKeyDao.kt`, and `AppDatabaseMigrations.kt`.

## Responsibilities

- Room database initialization, entity definitions, DAO query flows, and explicit schema migrations (`MIGRATION_1_2`).
- SQLite room schema version tracking and safety.

## Invariants

- Secrets in database tables MUST be stored encrypted using `SecretCipher`.
- Never enable `fallbackToDestructiveMigration` in production builds.
- Database operations must emit standard Kotlin `Flow` and run on `Dispatchers.IO`.
