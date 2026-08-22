# AGENTS.md — Documentation & Architecture Records

## Scope

This contract governs the `/docs` tree:
- `docs/adr/`: Architecture Decision Records capturing durable system decisions.
- `docs/agents/`: Agent domain layouts, issue tracking workflows, and triage labels.
- `docs/github-setup.md`: Repository setup and CI/CD documentation.

## Rules & Maintenance

- **ADRs:** Create numbered ADRs (`docs/adr/NNNN-*.md`) for significant, permanent architectural or cryptographic shifts.
- **Domain Context:** Ensure `docs/agents/domain.md` stays aligned with current `core/` and `feature/` module paths and root `CONTEXT.md`.
- **Formatting:** Keep docs concise, technical, and formatted in Markdown.
