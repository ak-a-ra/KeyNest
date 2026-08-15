# Issue Tracker Configuration

This repository uses local Markdown-based tracking and GitHub issue integration.

## Issue Locations & Formats

- **Local Roadmaps & Backlog**: `ROADMAP.md` and local task specifications.
- **GitHub Issues**: When synced to GitHub, standard issue templates and GitHub issue numbers are supported.

## Conventions

- Issues should define clear reproduction steps or acceptance criteria.
- Security-related issues must be tagged and prioritized immediately.

---

## 📋 Active Issue Backlog

### 🔴 ISSUE-8: Prevent Plaintext Fallback on Encryption Failure in `Cryptography.kt`
- **Status:** Open
- **Priority:** High (Security Invariant)
- **Description:** In `Cryptography.encrypt()`, the catch block currently returns raw `plainText` when AES cipher encryption fails. This violates the security invariant that secrets must never touch storage in plaintext.
- **Acceptance Criteria:**
  - `Cryptography.encrypt()` must never return the unencrypted input string upon failure.
  - Failures should either throw a secure exception or return an empty/error token.
  - Unit tests in `CryptographyTest.kt` verify no plaintext leakage under fault conditions.

### 🟠 ISSUE-9: Enforce Strict Hardware-Backed Encrypted Preferences in `VaultSecurity.kt`
- **Status:** Open
- **Priority:** High (Security Invariant)
- **Description:** `VaultSecurity.getPrefs()` falls back to unencrypted standard `SharedPreferences` if `EncryptedSharedPreferences.create()` fails. This could leak master PIN hashes and device salt in plaintext.
- **Acceptance Criteria:**
  - Eliminate unencrypted fallback or handle initialization errors with graceful degraded read-only / locked state.
  - Verify all sensitive preference operations require valid master key encryption.

### 🟡 ISSUE-10: Deprecate Legacy `String.hashCode()` PIN Verification
- **Status:** Open
- **Priority:** Medium (Code Hygiene & Standards)
- **Description:** `VaultSecurity.generateLegacyHash` relies on `String.hashCode()` for backward compatibility.
- **Acceptance Criteria:**
  - Mark `generateLegacyHash()` with `@Deprecated` and establish a sunset timeline.
  - Ensure modern SHA-256 with per-device salt remains the sole standard.

---

## ✅ Completed Issues

### 🟢 ISSUE-1: Legacy PIN Hash Compatibility
- **Status:** Resolved
- **Priority:** High
- **Description:** Legacy PIN verification used unsafe fallback String.hashCode().
- **Resolution:** Isolated legacy hash check, added automated seamless upgrade path to modern SHA-256 hash upon successful login, and added unit test coverage.

### 🟢 ISSUE-2: Cryptography IV Byte Array Allocations
- **Status:** Resolved
- **Priority:** Medium
- **Description:** Redundant ByteArray allocations and verbose System.arraycopy in Cryptography.kt encryption/decryption loops.
- **Resolution:** Refactored IV concatenation and slice extraction using native Kotlin array operations (`iv + encryptedBytes` and `.copyOfRange`).

### 🟢 ISSUE-3: Persist `lastSelfCopiedKey` across restarts
- **Status:** Resolved
- **Priority:** Low
- **Description:** `lastSelfCopiedKey` was in-memory only and lost on app restarts.
- **Resolution:** Persisted `lastSelfCopiedKey` in `EncryptedSharedPreferences` via `VaultSecurity.kt`, ensuring self-copied keys are recognized and ignored after app re-launches.

### 🟢 ISSUE-4: Simplify `AddEditKeySheet.kt` state initialization
- **Status:** Resolved
- **Priority:** Low
- **Description:** Complex nested ternary and Elvis operator expressions during sheet state initialization.
- **Resolution:** Refactored into clean, readable `when` expressions for `selectedProvider` and `title`.\n\n### 🟢 ISSUE-5: Decryption Performance Bottleneck in Repository
- **Status:** Resolved
- **Priority:** High
- **Description:** Every `getAllKeys()` Flow emission re-decrypted every key from SQLite, causing UI frame drops during search/filtering.
- **Resolution:** Introduced thread-safe `ConcurrentHashMap` decryption cache in `ApiKeyRepository.kt` keyed by ciphertext.

### 🟢 ISSUE-6: Duplicate Label Overwrite Protection
- **Status:** Resolved
- **Priority:** High
- **Description:** Saving a key with an existing label caused silent overwrites and data loss.
- **Resolution:** Added warning banner in `AddEditKeySheet.kt` when label already exists to protect data integrity.

### 🟢 ISSUE-7: Hardcoded Cryptographic Salt
- **Status:** Resolved
- **Priority:** High
- **Description:** Hardcoded salt string used in PIN hashing.
- **Resolution:** Replaced with a 16-byte secure random per-device salt generated and stored in `EncryptedSharedPreferences`, with automatic backward compatibility migration.

### 🟢 KN-01: Encrypt SQLite Database / Field-Level Encryption
- **Status:** Resolved
- **Priority:** High
- **Description:** Encrypted `apiKey` and `secretKey` fields transparently in `ApiKeyRepository` utilizing standard Android Keystore AES/GCM/NoPadding cryptography.
- **Resolution:** Added a native `Cryptography.kt` helper. Encrypts keys upon insertion/updates and decrypts on query retrieval with graceful backward-compatible fallback.

### 🟢 KN-02: Upgrade Settings to EncryptedSharedPreferences
- **Status:** Resolved
- **Priority:** Medium
- **Description:** Upgraded general preference store to Jetpack Security's `EncryptedSharedPreferences`.
- **Resolution:** Added `androidx.security:security-crypto` library, refactored `VaultSecurity.getPrefs()` to initialize `EncryptedSharedPreferences` utilizing Android Keystore master keys.

### 🟢 KN-04: Test-Drive Bug Diagnosis & Security Regression Test Suite
- **Status:** Resolved
- **Priority:** High
- **Description:** Implemented disciplined bug diagnosis loop and test suite verifying hardware-backed Keystore security invariants, entropy analysis, provider detection patterns, and .env parser/exporter edge cases.
- **Resolution:** Added `VaultSecurityTest.kt` (PIN lifecycle, key masking, provider signature detection, Shannon entropy calculations, key generators) and `VaultDotEnvTest.kt` (.env export/parse roundtrip, comment stripping, environment mapping). All tests verified passing in local JVM unit testing.
