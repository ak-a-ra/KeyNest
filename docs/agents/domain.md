# Domain Documentation Layout

## Layout

This is a single-context Android application.

- Root context: [`CONTEXT.md`](../../CONTEXT.md)
- Architecture decisions: `docs/adr/`
- Agent governance: [`AGENTS.md`](../../AGENTS.md)
- Product overview: [`README.md`](../../README.md)
- Product direction: [`ROADMAP.md`](../../ROADMAP.md)

## Consumer rules

- Read `CONTEXT.md` before `diagnose`, `tdd`, or `improve-codebase-architecture` work.
- Read relevant ADRs before changing a documented architectural decision.
- Add a numbered ADR for durable, consequential decisions that affect architecture, storage, security, or migration behavior.
- Keep `CONTEXT.md` concise and update it when domain language, module boundaries, or durable invariants change.

## Module boundaries

- `app/src/main/java/com/example/data/db/`: Room database and DAO.
- `app/src/main/java/com/example/data/model/`: Domain data structures and provider presets.
- `app/src/main/java/com/example/data/repository/`: Encryption-aware vault persistence.
- `app/src/main/java/com/example/data/security/`: Keystore cryptography and encrypted preferences.
- `app/src/main/java/com/example/ui/`: Compose components, screens, theme, and ViewModels.
