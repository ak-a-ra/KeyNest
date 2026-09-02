# KeyNest Provider Vault — Remake Implementation Plan

## Overview
Remake KeyNest into an Agora-style AI Provider & Developer Key Vault. One Profile per Provider, Multi-Key storage with 1 Active key toggle, live connection testing, and Room DB (schema v3) with Keystore AES-256-GCM encryption.

---

### Phase 1: Data Architecture & Storage Layer (Room v3 + Keystore)
*Transition database schema and repository boundary from freeform items to structured Provider Profiles with encrypted multi-key payloads.*

- [ ] **Task 1.1: Provider Profile & Multi-Key Domain Models**
  - Define `ProviderProfile` & `ProviderKeyItem` data classes.
  - Define `ProviderPreset` enum with AI providers (`GEMINI`, `OPENAI`, `ANTHROPIC`, `DEEPSEEK`, `GROQ`, `OLLAMA`, `OPENROUTER`) and Dev Tools (`GITHUB`, `STRIPE`, `SUPABASE`, `AWS`, `CUSTOM`).
  - Add preset defaults (display names, default base URLs, auth header types).

- [ ] **Task 1.2: Room Entity & Database Schema v3 Migration**
  - Create `ProviderProfileEntity` Room entity (id, category, displayName, baseUrl, customHeadersJson, isActive, keysJson, activeKeyId, updatedAt).
  - Create `ProviderDao` with reactive Flow queries (`getAllProviders()`, `getProvider(id)`, `insertProvider()`, `deleteProvider()`).
  - Implement clean schema migration `MIGRATION_2_3` or fresh v3 initialization in `AppDatabase.kt`.

- [ ] **Task 1.3: Repository Multi-Key Encryption & Resolver**
  - Update `ApiKeyRepository` (or `ProviderRepository`) to serialize/deserialize `List<ProviderKeyItem>` via `SecretCipher` (AES-256-GCM).
  - Implement active key selection, individual key addition/deletion/update, and secure memory resolution for active keys.

---

### Phase 2: Live Connection Testing Engine
*Direct network probe engine to test provider keys against live endpoints without logging secrets.*

- [ ] **Task 2.1: ProviderConnectionTester Service**
  - Implement coroutine-based network probe engine using standard HTTP connections.
  - Support probe endpoints:
    - Gemini: `GET {baseUrl}/v1beta/models?key={apiKey}`
    - OpenAI: `GET {baseUrl}/v1/models` (Bearer token)
    - Anthropic: `GET {baseUrl}/v1/models` (`x-api-key`, `anthropic-version: 2023-06-01`)
    - DeepSeek / Groq / OpenRouter / Ollama probe endpoints.
  - Return typed result: `ConnectionResult.Success(latencyMs)` or `ConnectionResult.Failure(statusCode, message)`.

- [ ] **Task 2.2: Test Connection UI Feedback**
  - Add visual ping state (Loading spinner, 🟢 "Connected (142ms)", 🔴 "HTTP 401: Invalid Key").

---

### Phase 3: Agora-Style UI & Screen Remake
*Replace freeform cards with clean, scannable Provider Cards and dedicated Provider Config Sheets.*

- [ ] **Task 3.1: Provider Card Component (`ProviderCard.kt`)**
  - Brand header (icon, provider name, category chip).
  - Endpoint URL pill (e.g. `https://api.openai.com/v1`).
  - Key status badge (🟢 `Configured (N keys)` with masked active token `sk-...8f1a` vs ⚪ `Not Set`).
  - 1-tap quick actions: Copy Active Key, Test Connection ping button, Active toggle switch.

- [ ] **Task 3.2: Main Vault Screen (`ProviderHomeScreen.kt`)**
  - Top Bar: "Providers", Search icon, Add Custom button, Export, Master PIN lock.
  - Filter Tabs: `All`, `AI Providers`, `Developer Keys`.
  - Grouped feeds: Configured Providers vs Available Presets.

- [ ] **Task 3.3: Provider Detail & Config Sheet (`ProviderConfigSheet.kt`)**
  - Base URL field (editable with "Reset to Default" button).
  - Custom Headers / Organization ID inputs.
  - Multi-Key Section:
    - Radio button selector for **Active Key**.
    - Masked secret field (`••••••••`) with Show/Hide toggle.
    - Key label editor ("Production", "Staging", "Dev").
    - "+ Add Key" button with clipboard paste.
  - Action row: "Test Connection" button, "Export .env", Save / Delete.

---

### Phase 4: Exporters, Backups & Verification
*Ensure seamless developer export workflows, backup compatibility, and 100% test coverage.*

- [ ] **Task 4.1: Active Key `.env` Exporter & Code Snippets**
  - Generate clean `.env` format from all active provider keys:
    ```env
    GEMINI_API_KEY=AIzaSy...
    OPENAI_API_KEY=sk-...
    ANTHROPIC_API_KEY=sk-ant-...
    ```
  - Developer code snippet generator (cURL, Python requests, TypeScript, Kotlin).

- [ ] **Task 4.2: Encrypted `.keynest` Backup Engine Update**
  - Update `VaultBackupCrypto` to serialize/restore v3 provider profiles with PBKDF2 + AES-256-GCM.

- [ ] **Task 4.3: Test Suite & Verification Gate**
  - Unit tests for repository multi-key encryption and active key resolution.
  - Network probe mock tests.
  - Robolectric UI flow tests.
  - Verify `compile_applet` and `./gradlew testDebugUnitTest` 100% green.
