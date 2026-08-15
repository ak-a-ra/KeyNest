# Issue Tracker

## System of record

GitHub Issues in [`ak-a-ra/KeyNest`](https://github.com/ak-a-ra/KeyNest/issues) are the sole issue tracker. Use the authenticated `gh` CLI for issue creation, updates, labels, and comments.

`ROADMAP.md` is planning context only. It is not an issue tracker and must not duplicate issue lifecycle state.

## Creating issues

- Include a concise problem statement, reproduction steps when applicable, and testable acceptance criteria.
- Before creating an issue, search open and closed GitHub Issues for duplicates.
- Apply `needs-triage` to every newly created issue.
- Apply `security` to security findings and prioritize them immediately.

## Moving issues through triage

Use the label mapping in `triage-labels.md`. After a finding is reproduced or its scope is validated, replace `needs-triage` with `ready-for-agent` unless it requires a human-only decision or verification.

## Issue references

Use GitHub issue numbers and URLs in plans, commits, PRs, and durable documentation. Do not maintain a separate local backlog of GitHub issue copies.
