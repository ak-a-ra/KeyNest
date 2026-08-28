# AGENTS.md — Features & Presentation

## Scope

This contract governs `com.example.feature.*`:
- `feature/vault`: Home screen (`VaultHomeScreen`), adaptive list-detail scaffold, drawer navigation, API key cards, empty states, trash bin view (`VaultTrashView`), and `VaultViewModel`.
- `feature/keymanagement`: Add/Edit sheet (`AddEditKeySheet`), form sections, key detail view (`KeyDetailSheet`), generator sheet (`KeyGeneratorSheet`), expiration cards.
- `feature/search`: Real-time query screen (`SearchScreen`), category/tag/color exploration hub.
- `feature/export`: `.env` import/export sheet (`DotEnvExportSheet`) and encrypted backup sheet (`VaultBackupSheet`).
- `feature/settings`: PIN lock authentication screen (`PinLockScreen`), security audit sheet (`SecurityAuditSheet`).

## UI & Accessibility Rules

- **Material Design 3:** Adhere strictly to M3 guidelines, dynamic colors, and contrast-safe surface tones (Google Keep tints).
- **Accessibility:** Minimum touch target size 48x48 dp for all interactive elements. Every interactive icon/button MUST have `Modifier.testTag(...)`.
- **Sensitive Clipboard:** Mask secrets by default with `PasswordVisualTransformation`; attach `EXTRA_IS_SENSITIVE` to clipboard copies.
- **Unidirectional Data Flow (UDF):** UI screens consume `StateFlow` from `VaultViewModel` and emit events upward.
- **Adaptive Layouts:** Support compact screens and tablet/foldable dual-pane views cleanly via `ListDetailPaneScaffold`.

## Child DOX Index

- [`vault/AGENTS.md`](vault/AGENTS.md) — Primary vault view, search bar, list/grid feed, drawer, trash bin, and ViewModel.
- [`keymanagement/AGENTS.md`](keymanagement/AGENTS.md) — Add/Edit key sheet, extra secret fields, provider auto-detection, key details, and generator.
- [`search/AGENTS.md`](search/AGENTS.md) — Real-time debounced search screen and dynamic tag filter hub.
- [`export/AGENTS.md`](export/AGENTS.md) — `.env` import/export sheet and encrypted `.keynest` backup sheet.
- [`settings/AGENTS.md`](settings/AGENTS.md) — Master PIN authentication, PIN settings sheet, and security audit sheet.
