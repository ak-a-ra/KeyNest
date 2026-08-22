# AGENTS.md — Features & Presentation

## Scope

This contract governs `com.example.feature.*`:
- `feature/vault`: Home screen (`VaultHomeScreen`), adaptive list-detail scaffold, drawer navigation, API key cards, empty states, trash bin view (`VaultTrashView`), and `VaultViewModel`.
- `feature/keymanagement`: Add/Edit sheet (`AddEditKeySheet`), form sections, key detail view (`KeyDetailSheet`), generator sheet (`KeyGeneratorSheet`), expiration cards.
- `feature/search`: Real-time query screen (`SearchScreen`), category/tag/color exploration hub.
- `feature/export`: Developer code snippet generator (`DeveloperCodeExporterScreen`), `.env` import/export sheet (`DotEnvExportSheet`), and encrypted backup sheet (`VaultBackupSheet`).
- `feature/settings`: PIN lock authentication screen (`PinLockScreen`), security audit sheet (`SecurityAuditSheet`).

## UI & Accessibility Rules

- **Material Design 3:** Adhere strictly to M3 guidelines, dynamic colors, and contrast-safe surface tones (Google Keep tints).
- **Accessibility:** Minimum touch target size 48x48 dp for all interactive elements. Every interactive icon/button MUST have `Modifier.testTag(...)`.
- **Sensitive Clipboard:** Mask secrets by default with `PasswordVisualTransformation`; attach `EXTRA_IS_SENSITIVE` to clipboard copies.
- **Unidirectional Data Flow (UDF):** UI screens consume `StateFlow` from `VaultViewModel` and emit events upward.
- **Adaptive Layouts:** Support compact screens and tablet/foldable dual-pane views cleanly via `ListDetailPaneScaffold`.
