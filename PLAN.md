# KeyNest Scalable System Architecture & Implementation Plan

## 1. System Architecture (High-Scale, Offline-First, KMP-Ready)

KeyNest is architected as an **Offline-First, Zero-Knowledge Developer Secret & AI Provider Vault**. The system decouples platform-specific hardware security modules from the domain logic to prepare for high-scale local execution, multi-workspace isolation, and cross-platform Kotlin Multiplatform (KMP) deployment (Android, Desktop, macOS/iOS).

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Presentation Layer (Jetpack Compose / M3)              │
│   Vault UI  │  Provider Hub  │  Multi-Key Switcher  │  Diagnostics & Export │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ StateFlow / Actions
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                  Application / Use Case Layer (CQRS-Lite)                   │
│   Query: GetActiveKeys, SearchIndex     │  Command: RotateKey, EncryptPayload│
└──────────────────┬───────────────────────────────────┬──────────────────────┘
                   │                                   │
┌──────────────────▼──────────────────┐ ┌──────────────▼──────────────────────┐
│  Domain Layer (Pure Kotlin Core)    │ │   Security Boundary (Zero-Leak)      │
│  - Workspace & Provider Aggregates  │ │   - SecretCipher (AES-256-GCM)       │
│  - Multi-Key & Active State Machine │ │   - StrongBox / Hardware Keystore    │
│  - Hybrid Logical Clock (HLC) Sync  │ │   - Secure Memory Buffer (CharArray) │
└──────────────────┬──────────────────┘ └──────────────┬──────────────────────┘
                   │                                   │
┌──────────────────▼───────────────────────────────────▼──────────────────────┐
│              Data & Infrastructure Layer (Multi-Tier Storage)               │
│   Room / SQLite DB  │  FTS5 Blind Index  │  L1 Crypto Cache  │  SAF / File  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ (Faraway E2EE Sync)
┌──────────────────────────────────────▼──────────────────────────────────────┐
│                 Outbox Sync Engine & Untrusted Relay Gateway                │
│    ChaCha20-Poly1305 Encrypted Deltas │ CRDT Merge │ WebDAV / Cloud Relay   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Architectural Principles:
1. **Zero-Knowledge Hardware-Backed Boundary**: No secret plaintext is ever stored unencrypted on disk or transmitted over the wire. Plaintext exists strictly in ephemeral, zero-wiped memory buffers during active user operations.
2. **CQRS-Lite Segregation**:
   - **Query Path**: Reactive SQLite streams (`Flow<List<ProviderProfile>>`) mapped through an L1 AES hardware-decryption cache for 60fps Compose rendering.
   - **Command Path**: Strict atomic transactions updating local Room SQLite tables and staging encrypted mutation deltas into an append-only `sync_outbox`.
3. **Local-First with Asynchronous Delta Sync**: The device database is the primary source of truth. Remote synchronization operations are idempotent and non-blocking.

---

## 2. Component Structure (Modular Bounded Contexts)

The codebase is organized into decoupled bounded contexts to ensure modular compilation, testing, and multiplatform migration:

```
com.example/
├── core/
│   ├── security/             # Cryptographic engine and hardware abstraction
│   │   ├── SecretCipher.kt            # Keystore AES-256-GCM implementation
│   │   ├── VaultSecurity.kt           # EncryptedSharedPreferences & PIN protection
│   │   ├── SecureMemory.kt            # CharArray zeroization and heap protections
│   │   └── VaultBackupCrypto.kt       # PBKDF2 (100k rounds) + GCM export/import
│   ├── database/             # Relational persistence & indexing
│   │   ├── AppDatabase.kt             # Room database instance & migrations
│   │   ├── ProviderDao.kt             # Provider & key relational queries
│   │   ├── WorkspaceDao.kt            # Multi-workspace partition queries
│   │   ├── FtsSearchDao.kt            # FTS5 full-text blind search index
│   │   └── SyncOutboxDao.kt           # E2EE mutation delta staging
│   ├── model/                # Pure domain entities and value objects
│   │   ├── ProviderProfile.kt         # Provider entity (OpenAI, Gemini, Custom)
│   │   ├── ProviderKeyItem.kt         # Secret key metadata & active state
│   │   ├── Workspace.kt               # Partition boundary (Personal, Work, Client)
│   │   └── SyncDelta.kt               # Encrypted CRDT change envelope
│   ├── repository/           # Data orchestration & decryption caching
│   │   ├── ProviderRepository.kt      # Provider CRUD + L1 hardware-crypto cache
│   │   ├── WorkspaceRepository.kt     # Multi-tenant workspace management
│   │   └── SyncRepository.kt          # Delta generation & conflict resolution
│   ├── network/              # Non-egress diagnostic probes
│   │   └── ProviderConnectionTester.kt# Endpoint ping, latency, and status probe
│   ├── files/                # Storage Access Framework (SAF)
│   │   └── VaultFileManager.kt        # Scoped document export/import (.env, .keynest)
│   └── designsystem/         # Material Design 3 tokens, tints, and components
└── feature/
    ├── vault/                # Main home feed, provider cards, search bar
    ├── keymanagement/        # Add/edit sheets, multi-key radio toggle, generator
    ├── workspaces/           # Workspace switcher and project scoping
    ├── search/               # Debounced tag and provider search
    ├── export/               # Developer snippets, .env generator, backup sheet
    └── settings/             # PIN authentication, biometric gate, security audit
```

---

## 3. Data Flow (End-to-End Reactive Pipeline)

```
[ User Action: Select Active Key ]
               │
               ▼
[ ViewModel: onSelectActiveKey(profileId, keyId) ]
               │
               ▼
[ ProviderRepository: updateActiveKey() ]
               │
               ├──────────────────────────────────────────┐
               ▼ (DB Transaction)                         ▼ (Sync Pipeline)
[ SQLite Database: provider_profiles ]       [ SyncOutbox: Append Delta ]
               │                                          │
               ▼ (Flow Emission)                          ▼
[ Room Emits Updated Entity Stream ]         [ Async Sync Relay Worker ]
               │
               ▼
[ Repository JIT Mapping & L1 Decryption Cache ]
  - Cache Hit: Return cached decrypted model immediately
  - Cache Miss: Keystore AES-GCM decrypt, populate L1, return model
               │
               ▼
[ ViewModel: UiState StateFlow Pipeline ]
               │
               ▼
[ Compose UI: ProviderCard Recomposes with Zero Lag ]
```

### Ephemeral Secret Lifetime:
1. **Input Phase**: Key entry field reads characters into `CharArray`.
2. **Encryption Phase**: `SecretCipher.encrypt()` wraps payload in AES-GCM envelope; `CharArray` is immediately overwritten with `\u0000`.
3. **Storage Phase**: Only ciphertext (Base64 IV + Ciphertext + Tag) is persisted in Room SQLite.
4. **Copy/Reveal Phase**: User explicitly requests secret reveal/copy -> Decrypted on demand -> Loaded into Android Clipboard marked with `ClipDescription.EXTRA_IS_SENSITIVE` -> Automatically scrubbed after 30 seconds.

---

## 4. API Design (Internal Interfaces & Faraway Protocols)

### 4.1. Core Cryptographic Boundary (`core.security`)
```kotlin
interface SecretCipher {
    @Throws(SecretCipherException::class)
    fun encrypt(plaintext: String): String
    
    @Throws(SecretCipherException::class)
    fun decrypt(ciphertext: String): String
}

interface SecureMemoryBuffer : AutoCloseable {
    val characters: CharArray
    fun clear()
}
```

### 4.2. Provider Gateway & Telemetry (`core.network`)
```kotlin
interface ProviderConnectionGateway {
    suspend fun probeEndpoint(
        baseUrl: String,
        apiKey: String,
        authType: ProviderAuthType
    ): ProbeTelemetryResult
    
    suspend fun introspectModels(
        baseUrl: String,
        apiKey: String,
        authType: ProviderAuthType
    ): Result<List<String>>
}

data class ProbeTelemetryResult(
    val isReachable: Boolean,
    val httpStatusCode: Int,
    val latencyMs: Long,
    val errorMessage: String? = null
)
```

### 4.3. Faraway E2EE Sync Protocol (Self-Hosted / Relay Specification)
```http
POST /api/v1/sync/push
Headers:
  Authorization: Bearer <Ephemeral-Device-Token>
  X-KeyNest-Vault-ID: <Hashed-Vault-UUID>
Body:
{
  "client_hlc": "2026-09-02T12:00:00.000Z-0001",
  "encrypted_deltas": [
    {
      "entity_type": "provider_profile",
      "entity_id": "uuid-v4",
      "operation": "UPSERT",
      "version": 4,
      "payload_ciphertext": "<ChaCha20-Poly1305-Base64>",
      "payload_nonce": "<Nonce-Base64>"
    }
  ]
}

GET /api/v1/sync/pull?since_hlc=2026-09-02T11:00:00.000Z-0000
Response:
{
  "server_hlc": "2026-09-02T12:05:00.000Z-0003",
  "deltas": [...]
}
```

---

## 5. Database Schema (Room SQLite with Multi-Tenant & Sync Staging)

```sql
-- Workspaces Partition Table
CREATE TABLE workspaces (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    icon_name TEXT NOT NULL DEFAULT 'folder',
    color_tint INTEGER NOT NULL DEFAULT 0,
    is_default INTEGER NOT NULL DEFAULT 0,
    created_at INTEGER NOT NULL
);

-- Provider Profiles Table
CREATE TABLE provider_profiles (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    workspace_id TEXT NOT NULL DEFAULT 'default_workspace',
    category TEXT NOT NULL,
    displayName TEXT NOT NULL,
    baseUrl TEXT NOT NULL,
    customHeadersJson TEXT NOT NULL,
    keysJson TEXT NOT NULL,            -- Encrypted List<ProviderKeyItem> via Keystore
    activeKeyId TEXT NOT NULL,         -- Pointer to the active key UUID
    isPinned INTEGER NOT NULL DEFAULT 0,
    updatedAt INTEGER NOT NULL,
    FOREIGN KEY(workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE
);

CREATE INDEX idx_provider_workspace ON provider_profiles(workspace_id);
CREATE INDEX idx_provider_pinned ON provider_profiles(isPinned);

-- Blind Search Full-Text Virtual Table (Zero Plaintext Secrets)
CREATE VIRTUAL TABLE fts_providers USING fts5(
    displayName,
    category,
    baseUrl,
    content='provider_profiles',
    content_rowid='id'
);

-- E2EE Sync Outbox Staging Queue
CREATE TABLE sync_outbox (
    change_id TEXT PRIMARY KEY NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    operation TEXT NOT NULL,           -- 'UPSERT' | 'DELETE'
    encrypted_patch_blob TEXT NOT NULL,-- Encrypted JSON patch
    hlc_timestamp TEXT NOT NULL,       -- Hybrid Logical Clock ISO-Counter
    sync_status TEXT NOT NULL          -- 'PENDING' | 'SYNCED' | 'FAILED'
);

CREATE INDEX idx_sync_outbox_status ON sync_outbox(sync_status);
```

---

## 6. Caching Strategy (Tiered L1/L2/L3 Architecture)

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Caching Tier Architecture                       │
├─────────────────┬──────────────────┬──────────────┬────────────────────┤
│ Tier            │ Target Data      │ Strategy     │ Invalidation / TTL │
├─────────────────┼──────────────────┼──────────────┼────────────────────┤
│ L1 Memory Cache │ Hardware AES     │ LRU Map      │ Key-change miss or │
│                 │ Decrypted String │ (Max 128)    │ Inactivity scrub   │
├─────────────────┼──────────────────┼──────────────┼────────────────────┤
│ L2 State Cache  │ Deserialized     │ Memoized     │ Room Entity Flow   │
│                 │ Key Items List   │ Fast-Path    │ Change Notification│
├─────────────────┼──────────────────┼──────────────┼────────────────────┤
│ L3 Probe Cache  │ Live Endpoint    │ Positive &   │ 5-minute sliding   │
│                 │ Latency & Status │ Negative TTL │ window per URL/key │
└─────────────────┴──────────────────┴──────────────┴────────────────────┘
```

1. **L1 Hardware Keystore Decryption Cache**:
   - **Implementation**: `ConcurrentHashMap<String, String>` mapping raw encrypted Base64 ciphertexts to their decrypted values.
   - **Performance Benefit**: Android KeyStore hardware cryptographic calls (TEE/StrongBox) impose 5–25ms latency per operation. L1 caching eliminates this bottleneck during fast LazyColumn scrolling, guaranteeing 60fps UI rendering.
   - **Zeroization & Invalidation**:
     - Automatically evicts old items when reaching capacity (128 entries).
     - On app backgrounding (`Lifecycle.Event.ON_STOP`) or screen lock (`Intent.ACTION_SCREEN_OFF`), the cache triggers `.clear()`, ensuring plaintext keys never reside in idle RAM.
2. **L2 Prepared State & Derived State Cache**:
   - Provider presets, tag extraction, and active key resolutions are cached with `derivedStateOf` in Compose and `distinctUntilChanged()` in ViewModels, preventing unnecessary recomposition passes.
3. **L3 Network Diagnostics TTL Cache**:
   - Probing an AI provider API endpoint (e.g., `GET /v1/models`) consumes network data and can trigger provider rate limits.
   - Probe results are cached for 300 seconds (5 minutes) keyed by `sha256(baseUrl + apiKey)`. Users can bypass this cache with an explicit pull-to-refresh or tap on the "Re-test" button.

