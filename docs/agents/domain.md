# Domain Documentation Layout

## Project Architecture & Context

- **Layout Style**: Single-context Android mobile application.
- **Context & Architecture**: `README.md`
- **Roadmap & Milestones**: `ROADMAP.md`
- **Agent Governance & Rules**: `AGENTS.md`

## Module Boundaries

- `app/src/main/java/com/example/data/`:
  - `db/`: Room database definitions (`AppDatabase.kt`, `ApiKeyDao.kt`).
  - `model/`: Domain data structures (`ApiKeyItem.kt`, `ProviderPreset.kt`).
  - `repository/`: Data layer abstraction (`ApiKeyRepository.kt`).
  - `security/`: Hardware-backed encryption (`VaultSecurity.kt`, Android Keystore, EncryptedSharedPreferences).
- `app/src/main/java/com/example/ui/`:
  - `components/`: Reusable UI elements (`VaultComponents.kt`).
  - `screens/`: Feature screens (`VaultHomeScreen.kt`, `AddEditKeySheet.kt`, `KeyDetailSheet.kt`, `PinLockScreen.kt`, `DotEnvExportSheet.kt`, `KeyGeneratorSheet.kt`).
  - `theme/`: Material 3 theme and tokens (`Theme.kt`, `Color.kt`, `Type.kt`).
  - `viewmodel/`: Reactive presentation logic (`VaultViewModel.kt`).

## OS Compatibility & Android 13+ Guarantees

- **Target SDK**: API 36 (Android 16) with full backward compatibility to Android 7.0+ (API 24) and optimized for Android 13+ (API 33+).
- **Android 13+ Features**:
  - `ClipDescription.EXTRA_IS_SENSITIVE`: Automatically attached to prevent sensitive API key leaks into the OS clipboard editor overlay.
  - Jetpack Compose Edge-to-Edge: Insets-aware layout integration with automatic system bar contrast.
  - Hardware Keystore & AES-GCM: Android 13+ Keymaster/KeyMint hardware security support.

## Deep Module & Architecture Principles

- **Deep Modules**: `VaultSecurity` and `ApiKeyRepository` expose concise, high-leverage interfaces hiding cryptographic, database, and entropy calculation complexity.
- **Seams & Decoupling**: ViewModels interact only with the repository and security models without binding directly to Room or raw database cursors.
- **YAGNI Compliance**: Minimal moving parts, standard library usage, zero redundant intermediary abstractions.

## Rules & Decisions

- New architectural decisions should be documented in this directory or linked from `README.md`.
- Security invariants defined in `AGENTS.md` are binding across all modules.
- **UI/UX Design Intelligence**: `ui-ux-pro-max` skill is configured at `/.agents/skills/ui-ux-pro-max/` (and `/skills/user_skills/ui_ux_pro_max/`) providing searchable datasets for color schemes, typography pairings, UX guidelines, and stack recommendations via `python3 /.agents/skills/ui-ux-pro-max/scripts/search.py`.
