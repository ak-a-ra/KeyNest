# AGENTS.md — App Module

## Scope

This contract governs the `/app` module: Android build configs, manifest, resources (`res/`), test suites (`src/test/`), and application entry points (`MainActivity.kt`, `KeyNestApplication.kt`).

## Responsibilities

- **Entry Points:** `KeyNestApplication` (Timber setup, crash handlers), `MainActivity` (Compose root, edge-to-edge, single-activity).
- **Resources (`res/`):** Material 3 themes, color palettes, vector icons, adaptive app icons (`ic_launcher_*`), XML backup configs.
- **Build & Signing:** `build.gradle.kts`, `proguard-rules.pro`, Room schema definitions (`schemas/`).
- **Tests (`src/test/`):** Robolectric JVM unit tests, Roborazzi screenshot tests, and cryptographic verification suites.

## Invariants & Rules

- **Namespace & ID:** Keep `namespace = "com.example"` unchanged; `applicationId` must stay unique.
- **Zero Plaintext Backups:** Keep `android:allowBackup="false"` and `android:fullBackupContent="false"` in `AndroidManifest.xml`.
- **Icon Integrity:** Custom adaptive launcher icon (`ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `ic_launcher_keynest.jpg`) must remain configured.
- **Test Integrity:** Never delete or bypass unit/Robolectric test suites. Verify `./gradlew test` passes on changes.

## Child DOX Index

- `src/main/java/com/example/core/AGENTS.md` — Database, security, models, repositories, files, util, designsystem.
- `src/main/java/com/example/feature/AGENTS.md` — Compose screens, feature sheets, ViewModels, adaptive UI.
