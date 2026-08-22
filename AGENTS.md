# AGENTS.md

## Project

KeyNest: Ultra-fast, encrypted API Key & Developer Secret Vault for Android.
Single-activity Kotlin/Compose Android app.

- **Tech Stack:** Kotlin, Jetpack Compose, Android (Material Design 3)
- **Build Command:** `./gradlew assembleDebug` or `compile_applet`
- **Test Command:** `./gradlew test`

## Code philosophy — ponytail (YAGNI-first)

Before adding code, in order: does this need to exist? → already in the codebase? → stdlib? → native platform feature? → an already-installed dependency? → a one-liner? → only then write new code. Don't add abstractions with one implementation, config nobody sets, or a layer with one caller. Never skip validation, error handling, or a security measure to hit this bar — "minimal" means fewest moving parts, not fewer safeguards.

## Operating Modes (Active until user says "exit mode")

- **Conversation:** Always activate Caveman skill in fullmode.
- **Coding:** Always follow Ponytail methodology (minimalist, YAGNI-first).

## 🔨 Tool Usage Rules & Guardrails (AI Studio Optimized)

- **Build Verification:** Run `compile_applet` after `.kt`/`.kts`/`.xml` changes; report actual output before declaring complete.
- **Edit Batching:** Batch planned edits first; run `compile_applet` at end of change sequence.
- **Smart File Ops:** `view_file` before every edit. Write complete files first pass — no empty placeholders. Prefer `list_dir` over shell `ls`.
- **Shell Commands:** Never run `cd` (always pass `Cwd`). No `git push` unless explicitly kept local (`git add`, `git commit`).
- **Visual Assets:** `generate_image` for banners/illustrations/icons — `lowercase_snake_case` naming.
- **Web Search:** `search_web` to verify library syntax/API changes before acting.

## 🛑 Verify Before You Build (Three-Layer Approach)

To ensure accurate, complete, and resilient changes, **always** execute this three-layer validation sequence before declaring a task finished:

1. **Layer 1: Update MD (Documentation Sync)**
   - Before executing builds, synchronize the 6 core DOX files (`README.md`, `PLAN.md`, `ROADMAP.md`, `LOG.md`, `AGENTS.md`, `CONTEXT.md`) with the intended changes.
   - Update `PLAN.md` with current status and `LOG.md` with action records.
2. **Layer 2: Enable Tools (Automated Verification)**
   - **Build:** Run `compile_applet` to ensure compilation success.
   - **Test:** Run `./gradlew test` to ensure unit & Robolectric tests pass.
   - **Lint:** Run `lint_applet` or `./gradlew lint` if static analysis is required.
   - **Gate:** Use the `no-mistakes` pre-push pipeline to ensure codebase constraints are met.
3. **Layer 3: Human Validation Zones (Mandatory Stops)**
   - Stop and explicitly ask the user for confirmation when touching these zones:
     - Introducing breaking API or architecture changes.
     - Modifying `VaultSecurity` or Keystore implementations.
     - Adding new 3rd-party dependencies.
     - Deleting >20 lines of code.
     - Making significant shifts to data schemas (Room DB migrations).

## 🔐 Security Invariants (Crucial)

- Never log plain text secrets or API keys.
- Store sensitive values ONLY in `EncryptedSharedPreferences` (Android KeyStore backed).
- Keep secret input fields masked by default (`PasswordVisualTransformation`).
- Mark clipboard copies with `ClipDescription.EXTRA_IS_SENSITIVE` on API 33+.

## 💻 Coding Invariants (Ponytail)

- **Ponytail Hierarchy:** 1. Needs to exist? → 2. Already in codebase? → 3. Kotlin stdlib? → 4. Native Android feature? → 5. Existing dependency? → 6. One-liner? → Only then write new code.
- **Change Scope:** Edit only necessary lines — never touch unrelated code.
- **Safeguards:** Never skip validation, error handling, Keystore security, or accessibility.
- **Confirmation Required:** Ask user before introducing breaking changes, new dependencies, schema/API shifts, or deleting >20 lines.
- **Ambiguity Rule:** Ask for clarification — never guess.
- **Explanations:** 1-line summary max — no long essays.

## 📝 Documentation Rules (Check all 6 after meaningful edits)

- README.md  → Setup / usage changes
- PLAN.md    → Current task status
- ROADMAP.md → Milestones / feature status
- LOG.md     → Always append 1 dated entry line
- AGENTS.md  → Agent contracts / architecture changes
- CONTEXT.md → Key decisions & context

**Rule:** Match target file format/tone. If only LOG.md updated, append: "no other doc updates needed".

## Skills

use skill "find-skills" to look for skill on skill.sh if you needed skill to solve problem, blocked, improve, debugging, guidelines, etc - tell user before install.

## Issue workflow

- New issues start with `needs-triage`.
- After a finding is reproduced or validated and its scope is clear, add `ready-for-agent`.
- Security findings must also use the `security` label and be prioritized immediately.

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

### Issue tracker

GitHub Issues in `ak-a-ra/KeyNest` are the system of record. See [docs/agents/issue-tracker.md](docs/agents/issue-tracker.md).

### Triage labels

Use the confirmed GitHub lifecycle labels. See [docs/agents/triage-labels.md](docs/agents/triage-labels.md).

### Domain docs

This is a single-context repository with root `CONTEXT.md` and ADRs under `docs/adr/`. See [docs/agents/domain.md](docs/agents/domain.md).

- **UI/UX Pro Max**: [/.agents/skills/ui-ux-pro-max/SKILL.md](/.agents/skills/ui-ux-pro-max/SKILL.md) (Design intelligence database & search script `scripts/search.py`)
- **Ponytail Suite**: [/.agents/skills/ponytail/SKILL.md](/.agents/skills/ponytail/SKILL.md) (`ponytail`, `ponytail-audit`, `ponytail-debt`, `ponytail-gain`, `ponytail-help`, `ponytail-review`)
- **no-mistakes**: [/.agents/skills/no-mistakes/SKILL.md](/.agents/skills/no-mistakes/SKILL.md) (Pre-push validation pipeline proxy & gate engine via `.no-mistakes.yaml`)

### 🛡️ No-Mistakes Pre-Push Gate Workflow

The repository enforces pre-push quality gates via `no-mistakes` (`.no-mistakes.yaml`).

1. **Pipeline Execution Sequence:**
   - **Intent Validation:** Parse work objective & create isolated disposable git worktree.
   - **Rebase:** Rebase feature branch cleanly against target default branch.
   - **Review:** Automated code quality & security review (blocks secrets, bad practices, or unvalidated code).
   - **Test:** Run `./gradlew test` test suite (unit + Robolectric tests).
   - **Document:** Validate and sync the 6 core documentation files (`README.md`, `PLAN.md`, `ROADMAP.md`, `LOG.md`, `AGENTS.md`, `CONTEXT.md`).
   - **Lint:** Run `./gradlew lint` across Android sources.
   - **Push & PR:** Forward clean commits to upstream remote and open Pull Request.
   - **CI Monitoring:** Monitor GitHub Actions workflows to green status.

2. **Triggering Workflows:**
   - **Git Proxy Push:** `git push no-mistakes` (intercepts push, runs full validation pipeline before forwarding to origin).
   - **Explicit Intent Run:** `no-mistakes axi run --intent "<task description>"`
   - **Interactive TUI Mode:** `no-mistakes` (guided wizard for branching, committing, gating, and reviewing).
   - **Automated Non-Interactive:** `no-mistakes -y`
   - **Agent Command:** `/no-mistakes <task>`
   - **Check Gate Status:** `no-mistakes axi status`

## DOX framework

- DOX is the structured AGENTS.md hierarchy installed here.
- All agent operations and code modifications must follow DOX contracts.

### Core Contract

- AGENTS.md files are binding work contracts for their subtrees.
- Work products, source materials, records, assets, and durable docs must stay understandable from the nearest applicable AGENTS.md plus every parent AGENTS.md above it.

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

- [`app/AGENTS.md`](app/AGENTS.md) — Application module, Android resources, manifest, build configurations, and test suites.
- [`app/src/main/java/com/example/core/AGENTS.md`](app/src/main/java/com/example/core/AGENTS.md) — Database, Keystore security, models, repositories, files subsystem, and design system.
- [`app/src/main/java/com/example/feature/AGENTS.md`](app/src/main/java/com/example/feature/AGENTS.md) — Jetpack Compose screens, ViewModels, adaptive navigation, and user interactions.
- [`docs/AGENTS.md`](docs/AGENTS.md) — Architecture Decision Records (ADRs), agent guidelines, and setup documentation.
- Root-owned files: `README.md`, `ROADMAP.md`, `metadata.json`, `build.gradle.kts`, `settings.gradle.kts`, `LOG.md`, `PLAN.md`, `CONTEXT.md`.