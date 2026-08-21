# KeyNest Roadmap 🗺️

This document outlines the product vision, planned milestones, and upcoming capabilities for KeyNest.

---

## 🎯 Vision
To be the fastest, safest, and most intuitive secret manager for mobile developers, AI engineers, and DevOps practitioners.

---

## 📅 Milestones

### Phase 1: Core Foundation & UI Polish (✅ Completed)
- [x] Android Keystore encryption layer via `EncryptedSharedPreferences` (AES-256 GCM)
- [x] Material 3 Developer Dark OLED Theme (`#0B0F19` Navy, `#111827` Slate, `#06B6D4` Cyan, `#10B981` Emerald, `#EF4444` Crimson)
- [x] Monospace Token Hierarchy for API Key Prefixes, Secret Masks (`sk-proj-••••••••4a2f`), and SHA-256 Checksum Fingerprints (prevents 0 vs O, l vs 1 ambiguity)
- [x] UI/UX Pro Max Design System Upgrades: Bento Box Vault Security Health Header & Key Entropy Gauge (Removed)
- [x] Google Keep (Notes) Style Floating Top Search Bar Banner with Modal Navigation Drawer layout (categories, environments, quick tools, theme, and PIN management)
- [x] Google Keep (Notes) Style Search Details Screen (types, labels, environments layout)
- [x] Google Keep (Notes) Style Capsule Filter Chip Bar with category/environment icons, checkmark indicators, and smooth horizontal scrolling (Removed)
- [x] One-Tap Copy Toast Banner featuring visual circular progress ring and 30-second sensitive clipboard memory auto-clear countdown
- [x] Smooth scale-and-fade blur-like reveal transition when pressing the unmask toggle
- [x] Live Provider Auto-Detection Badge transforming in real-time as user types API key prefixes (`sk-`, `AIza`, `gsk_`, `xai-`, `ghp_`, `AKIA`, etc.)
- [x] Multi-screen backstack routing via Jetpack Navigation Compose (Removed Bottom Navigation Tabs per Google Keep layout)
- [x] Adaptive Large Screen & Tablet Container Constraints (`Modifier.widthIn(max = 600.dp)`)
- [x] 1-Tap tactile copy with haptic feedback & auto-reset animations
- [x] Smart clipboard sniffing for OpenAI, Anthropic, Gemini, AWS, Stripe, etc.
- [x] Master PIN lock screen and vault protection
- [x] Secret entropy calculator and strength analysis
- [x] In-app random secret/token generator
- [x] `.env` format exporter & batch importer
- [x] Native Dark Mode, Light Mode, and System Theme custom sync & cycle toggle
- [x] Automated GitHub Actions Build & Release Workflow (Manual triggered, Debug APK)
- [x] Key Expiration & Rotation Status Material3 Card Component with 3-tier color-coded alerts (Fresh, Approaching Expiry, Overdue) and lifecycle progress visualization

---

### Phase 2: Enhanced Visuals & Secret Operations (🔄 Q3 2026)
- [x] **Google Keep 1-Tap Grid / List Toggle**: Seamless dynamic switching between 2-column staggered masonry and single-column detailed list feeds, with persisted layout preference in encrypted settings.
- [x] **Google Keep Pastel Color Palette Picker**: Keep-style bottom color-dot selector (Coral, Sand, Sage, Fog, Dusk, Storm, Blossom) for custom note card pastel tints.
- [x] **Developer Code Exporter Action Sheet**: 1-tap quick code snippet copy for `.env`, Shell `export`, `Authorization: Bearer <key>`, cURL, and Python `requests` headers.
- [x] **Archive & Soft-Delete Trash Bin**: Archive inactive keys and provide a 30-day recoverable Trash bin in navigation drawer with restore and permanent purge protection.
- [x] **Architecture Realignment (Obsidian-Scale)**:
  - [x] **Package by Feature Restructuring**: Migrate from monolithic layers to strict feature modules (`feature.vault`, `feature.export`, `core.security`).
  - [x] **Material 3 Adaptive UI**: Implement `ListDetailPaneScaffold` for responsive two-pane tablet and desktop layouts.
  - [ ] **Dedicated File Subsystem**: Isolate Storage Access Framework (SAF), URI permissions, and MIME handling into a strict `core.files` subsystem.
  - [ ] **Strict Unidirectional Data Flow (UDF)**: Rigid `UiState` and `UiAction` classes per feature for predictable, testable state management.
  - [ ] **DataStore for Preferences**: Migrate UI settings (layout preference, theme) to DataStore, reserving `EncryptedSharedPreferences` strictly for cryptographic keys.
- [ ] **App Icon & Logo Redesign**: Custom adaptive launcher icon with developer-centric cybersecurity branding (AES key vault / cyberpunk obsidian aesthetic) and clean monochrome vector layer.
- [ ] **Anti-Slop UI Craft Polish**:
  - [ ] **Semantic Status Accents**: Color-coded key health indicators (expiring warning accents, cyan active indicators) over decorative ambient background glows.
  - [ ] **Utility-Driven Empty States**: Quick 1-tap preset sample importer in empty vault view (e.g., "Import AWS / OpenAI Template").
  - [ ] **Technical Microcopy Precision**: Direct, action-oriented developer microcopy across empty states and security dialogs ("AES-256 KeyStore Vault • Zero Plaintext Logs").
- [ ] **Search Query Loading Indicator**: Subtle progress indicator / pulse animation while query filters large vaults to provide clear feedback during high-volume secret queries.
- [ ] **Dedicated SearchViewModel**: Dedicated SearchViewModel to handle real-time filter logic, debounced input pipelines, and manage the state of the secrets list as the user types.
- [ ] **Secure QR Code Sharing**: Ephemeral, encrypted QR code generation for quick peer-to-peer secret transfer between devices.

---

### Phase 3: Developer Workflows & Integrations (🚀 Q4 2026)
- [ ] **Encrypted Backup & Restore**: Encrypted single-file export (`.vault` container) protected by user-defined master passphrase.
- [ ] **Custom Provider Presets**: Allow users to define reusable provider templates with custom URL endpoints, headers, and docs.
- [ ] **CLI / ADB Companion Sync**: Lightweight local pairing to pipe secrets into development workstations over secure local channel without cloud reliance.
- [ ] **Tagging & Advanced Search**: Multi-tag filtering and regex-based search indexing.
- [ ] **Detailed Security Audit Log**: Historical log of key reveal/copy timestamps and secret age analytics.

---

### Later / Backlog (⏳ Future)
- [ ] **Biometric Security**: App lock gate + per-key biometric reveal (`BiometricPrompt` & `BiometricManager` integration).
- [ ] **Auto-Lock Timeout**: Immediate, 30s, 1m, 5m configurable background lock settings.
- [ ] **Secret Health & Rotation**: Expiration countdowns, weak key detector, aging alerts, and rotation notifications.

---

## 💡 Suggestions & Contributions
Have a feature request or security recommendation? Please open an issue or submit a pull request!

---

> 📌 **Note on Paused Features:** Key Generator and Security Audit sheets have been temporarily commented out (`//`) to minimize active app memory footprint and streamline navigation. They will be re-evaluated and re-added in a future release if memory impact remains lightweight.
