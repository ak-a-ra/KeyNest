# AGENTS.md — Core Repository Boundary

## Scope

Governs `com.example.core.repository`: `ApiKeyRepository.kt`.

## Responsibilities

- Data boundary isolating ViewModel layer from direct database/Room and crypto implementation details.
- Encrypting API keys on write and decrypting on read via injected `SecretCipher`.

## Invariants

- Per-row decryption recovery: undecryptable rows degrade to `UNDECRYPTABLE_PLACEHOLDER` to preserve healthy vault items.
- All repository methods must operate asynchronously using coroutines and Kotlin `Flow`.
