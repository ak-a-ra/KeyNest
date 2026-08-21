# KeyNest Feature Development Plan

## Scope: Search, Tags, Favorites, Grid/List Layout, Keep-Style Pastel Tints

---

### 1. 🔍 Real-Time Deep Search
*Enable instantaneous, zero-latency multi-field querying across the encrypted vault.*

- [x] **Task 1.1: Reactive Query Filtering in ViewModel & DAO**
  - **Description**: Expand `VaultViewModel` search query pipeline using Kotlin `StateFlow` matching titles, providers, categories, environments, tags, endpoint URLs, models/projects, organizations, and notes.
  - **Output**: Instant reactive list matching query without UI jank.

- [x] **Task 1.2: Search Screen UX & Query Highlighting**
  - **Description**: Provide 1-tap clear button (`Icons.Default.Clear`), active query match count badge ("X found"), empty search state with technical microcopy, and interactive Keep-style explorer (Types, Labels, Dynamic Vault Tags, Color Tints).

- [x] **Task 1.3: Partial & Prefix Secret Mask Search**
  - **Description**: Allow searching by prefix/suffix format (e.g. searching `...4f8c` or `sk-` matches key preview tokens safely without exposing plaintext in memory).

---

### 2. 🏷️ Multi-Tag Taxonomy System
*Organize API keys across workspaces, clients, microservices, and projects.*

- [ ] **Task 2.1: Comma-Separated & Chip Model Serialization**
  - **Description**: Provide robust parser/formatter in `ApiKeyItem` / `ApiKeyFormatting` for normalized tag arrays (`List<String>`).
  - **Validation**: Auto-trim whitespace, remove empty tags, prevent duplicates, cap max tag length to 20 chars.

- [ ] **Task 2.2: Add/Edit Sheet Tag Input Chip Field**
  - **Description**: Interactive chip-input component where pressing Enter, comma, or space turns text into a removable tag pill (`InputChip` with delete cross).
  - **Suggestions**: Show quick-add pills of existing tags previously used across the vault.

- [ ] **Task 2.3: Horizontal Tag Filter Carousel on Home Screen**
  - **Description**: Scrollable filter bar (`LazyRow`) with "All" + dynamically extracted unique tags sorted by frequency.
  - **Interaction**: Tap tag pill to toggle active filter state with visual indicator (`CyberCyan` border and filled pill).

---

### 3. ⭐ Favorites & Pinned Keys Architecture
*Keep critical production credentials and daily developer keys at the top of the vault.*

- [ ] **Task 3.1: Instant Pin/Unpin Database & State Mutation**
  - **Description**: Add `togglePin(id: Long)` to DAO & Repository with immediate atomic Room update (`UPDATE api_keys SET isPinned = NOT isPinned WHERE id = :id`).
  - **Optimistic State**: Fast UI feedback before disk commit.

- [ ] **Task 3.2: Pinned vs. Vault Sections Separation**
  - **Description**: Group feed into "📌 PINNED" and "ALL KEYS" sections when pinned items exist.
  - **Layout Handling**: Maintain grid/list coherence across both grouped sections.

- [ ] **Task 3.3: 1-Tap Quick Pin Affordances**
  - **Description**: Direct pin icon toggle in card action menu and prominent star/pin badge in card header.

---

### 4. 🔲 Grid vs. List Toggle View with Persistence
*Fluid Google Keep-style feed toggle between 2-column masonry grid and full-width list items.*

- [ ] **Task 4.1: Encrypted Preferences Layout Persistence**
  - **Description**: Persist `isGridLayout: Boolean` via `EncryptedSharedPreferences` / `DataStore` in `VaultSecurity`.
  - **Default**: Staggered 2-column grid on compact screens, adaptive on tablets.

- [ ] **Task 4.2: Top Bar Layout Toggle Button**
  - **Description**: Seamless icon switch between `Icons.Default.GridView` and `Icons.AutoMirrored.Filled.List` with animated icon rotation/fade.

- [ ] **Task 4.3: Adaptive Card Layout Renderers**
  - **Grid Card**: Compact vertical card with provider icon, masked token badge, environment pill, and bottom tag row.
  - **List Row**: Horizontal compact strip optimized for high-density scanning with 1-tap copy button and status pill.

---

### 5. 🎨 Google Keep Pastel Color Palette
*Distinguish API keys visually using pleasant, contrast-safe pastel background tones.*

- [ ] **Task 5.1: Pastel Preset Palette Definition**
  - **Tints**: Default Obsidian (`#18191E`), Coral (`#2E1E1E`), Sand (`#2E2718`), Sage (`#1C2A20`), Fog (`#1C252E`), Dusk (`#261D2E`), Storm (`#1E272E`), Blossom (`#2E1C27`).
  - **Storage**: Persist clean hex code in `ApiKeyItem.colorHex`.

- [ ] **Task 5.2: Bottom Sheet Color-Dot Selector**
  - **Description**: Horizontal row of 8 circular color swatches with checkmark on active color in `AddEditKeySheet`.
  - **Live Preview**: Card preview header reflects selected tint in real time.

- [ ] **Task 5.3: Contrast-Aware Surface & Border Styling**
  - **Description**: Compute adaptive borders (`color.copy(alpha = 0.35f)`) and high-contrast typography so all text (`TextPrimary`, `CyberCyan`) remains strictly WCAG AAA compliant against tinted cards.

### Architecture Update
- [x] Package by Feature Restructuring completed.
- [x] Material 3 Adaptive UI integrated (`ListDetailPaneScaffold`).
