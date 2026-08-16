# KeyNest Roadmap

KeyNest is an Android vault for API keys and developer secrets. It’s built for speed, security, and simplicity—no fluff, just a fast encrypted store with Material 3 polish.

---

## Vision

KeyNest aims to be the fastest, most secure, and easiest-to-use secret manager for mobile developers, AI engineers, and DevOps teams.

---

## Milestones

### Phase 1: Core & UI (✅ Done)
- [x] Encrypted storage: Android Keystore + `EncryptedSharedPreferences` (AES-256 GCM).
- [x] Dark OLED theme: `#0B0F19` (navy), `#111827` (slate), `#06B6D4` (cyan), `#10B981` (emerald), `#EF4444` (crimson).
- [x] Monospace formatting: API key prefixes (`sk-proj-••••••••4a2f`), masked secrets, and SHA-256 checksums to avoid confusing `0`/`O` or `l`/`1`.
- [x] Floating search bar and navigation drawer: Google Keep-style layout with categories, environments, quick tools, theme, and PIN settings.
- [x] One-tap copy: Visual feedback, 30-second clipboard auto-clear, and haptic confirmation.
- [x] Provider detection: Recognizes `sk-`, `AIza`, `gsk_`, `xai-`, `ghp_`, `AKIA`, and other common prefixes as you type.
- [x] PIN lock: Optional vault protection.
- [x] Entropy calculator: Checks key strength.
- [x] Key generator: Creates secure tokens, API keys, hex secrets, and UUIDs.
- [x] `.env` import/export: Batch import and export.
- [x] Theme sync: Dark, light, and system theme with a cycle toggle.
- [x] GitHub Actions: Manual-triggered debug APK builds and releases.
- [x] Expiry tracking: Material 3 cards with color-coded alerts (fresh, expiring, overdue) and progress indicators.

---

### Phase 2: Biometrics & Secret Ops (🔄 Q3 2026)
- [ ] **Grid/list toggle:** Switch between 2-column masonry and single-column list. Saves your preference.
- [ ] **Color picker:** Google Keep-style pastel dots (coral, sand, sage, fog, dusk, storm, blossom) for card tints.
- [ ] **Code exporter:** One-tap copy for `.env`, shell `export`, `Authorization: Bearer <key>`, cURL, and Python `requests`.
- [ ] **Archive & trash:** Archive inactive keys and a 30-day recoverable trash bin with restore/purge.
- [ ] **UI polish:**
  - [ ] **Status accents:** Color-coded key health (expiring = warning, active = cyan) without decorative glows.
  - [ ] **Empty states:** Quick-import templates (e.g., “Import AWS/OpenAI Template”) in empty vaults.
  - [ ] **Microcopy:** Direct, technical labels (“AES-256 KeyStore Vault • Zero Plaintext Logs”).
- [ ] **Biometric unlock:** Fingerprint and face unlock via Android `BiometricManager`.
- [ ] **Per-key biometric gate:** Require biometric auth before revealing or copying high-risk keys.
- [ ] **Auto-lock timeout:** Configurable background lock (instant, 30s, 1m, 5m).
- [ ] **Rotation reminders:** System notifications when keys exceed their rotation period (30/60/90 days).
- [ ] **QR sharing:** Ephemeral, encrypted QR codes for peer-to-peer secret transfer.

---

### Phase 3: Developer Workflows (🚀 Q4 2026)
- [ ] **Encrypted backup:** Single-file `.vault` export protected by a user passphrase.
- [ ] **Custom presets:** Define reusable provider templates with URLs, headers, and docs.
- [ ] **CLI/ADB sync:** Secure local pairing to pipe secrets into workstations—no cloud required.
- [ ] **Tagging & search:** Multi-tag filtering and regex search.
- [ ] **Audit log:** Tracks key reveal/copy timestamps and secret age.

---

## Contribute

Got a feature request or security idea? Open an issue or send a pull request.

---

> **Note:** The key generator and security audit sheets are currently disabled to save memory. They’ll return if the memory impact stays low.