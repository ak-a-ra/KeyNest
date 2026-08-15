# AGENTS.md

## Project

KeyNest: Ultra-fast, encrypted API Key & Developer Secret Vault for Android.
Single-activity Kotlin/Compose Android app.

- **Tech Stack:** Kotlin, Jetpack Compose, Android (Material Design 3)
- **Build Command:** `./gradlew assembleDebug` or `compile_applet`
- **Test Command:** `./gradlew test`

## Code philosophy — ponytail (YAGNI-first)

Before adding code, in order: does this need to exist? → already in the codebase? → stdlib? → native platform feature? → an already-installed dependency? → a one-liner? → only then write new code. Don't add abstractions with one implementation, config nobody sets, or a layer with one caller. Never skip validation, error handling, or a security measure to hit this bar — "minimal" means fewest moving parts, not fewer safeguards.

## Security invariants — do not regress these

- Key values are stored via `EncryptedSharedPreferences` only. Never swap in plain `SharedPreferences`, a flat file, or a database without an equivalent Keystore-backed encryption layer.
- `android:allowBackup="false"` stays false.
- The "API key" input field in `AddKeyDialog` / `AddEditKeySheet` stays masked by default (`PasswordVisualTransformation`) with an explicit reveal toggle — it was plaintext-on-screen before, that was a real bug, not a style choice.
- Clipboard writes stay flagged `ClipDescription.EXTRA_IS_SENSITIVE` on API 33+.
- Saving a label that already exists must warn before overwriting (SharedPreferences keys on label — a silent overwrite is silent data loss).
- Never log key values, even in debug builds.

## Agent Guidelines (Persona & Behavior)

- Adopt modern development practices (MVVM, Clean Architecture) within the minimal code constraints.
- Prioritize native platform libraries and existing dependencies over third-party libraries.
- Write clean, production-ready, self-documenting code.
- Strictly adhere to Material Design 3 guidelines and dynamic accessibility sizing.

## Code Style & Conventions

- **Language:** Kotlin exclusively for logic and UI.
- **Formatting:** 4-space indentation, strict type-safety.
- Prefer constructor injection over heavy dependency injection frameworks unless requested.

## Build / verify

- Verify build with `compile_applet` tool or `./gradlew assembleDebug`.
- Maintain clean incremental builds with zero compilation errors or unresolved dependencies.

## Agent skills

- **Issue tracker**: [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md)
- **Triage labels**: [docs/agents/triage-labels.md](docs/agents/triage-labels.md)
- **Domain layout**: [docs/agents/domain.md](docs/agents/domain.md)
- **UI/UX Pro Max**: [/.agents/skills/ui-ux-pro-max/SKILL.md](/.agents/skills/ui-ux-pro-max/SKILL.md) (Design intelligence database & search script `scripts/search.py`)
- **Ponytail Suite**: [/.agents/skills/ponytail/SKILL.md](/.agents/skills/ponytail/SKILL.md) (`ponytail`, `ponytail-audit`, `ponytail-debt`, `ponytail-gain`, `ponytail-help`, `ponytail-review`)

## DOX framework

- DOX is the structured AGENTS.md hierarchy installed here.
- All agent operations and code modifications must follow DOX contracts.

### Core Contract
- AGENTS.md files are binding work contracts for their subtrees.
- Work products, source materials, instructions, records, assets, and durable docs must stay understandable from the nearest applicable AGENTS.md plus every parent AGENTS.md above it.

### Read Before Editing
1. Read the root AGENTS.md.
2. Identify every file or folder expected to touch.
3. Walk from the repository root to each target path.
4. Read every AGENTS.md found along each route.
5. If a parent AGENTS.md lists a child AGENTS.md whose scope contains the path, read that child and continue from there.
6. Use the nearest AGENTS.md as the local contract and parent docs for repo-wide rules.

### Update After Editing
Every meaningful change requires a DOX pass before the task is complete.
Update the closest owning AGENTS.md when a change affects:
- purpose, scope, ownership, or responsibilities
- durable structure, contracts, workflows, or operating rules
- required inputs, outputs, permissions, constraints, side effects, or artifacts
- user preferences about behavior, communication, process, organization, or quality

### Child DOX Index
- No child AGENTS.md files are needed for the current repository structure.
- Root-owned files: `README.md`, `ROADMAP.md`, `metadata.json`, `build.gradle.kts`, `settings.gradle.kts`, `app/` hierarchy.
