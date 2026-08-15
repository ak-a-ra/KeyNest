---
name: code-review
description: Review changes since a fixed point (commit, branch, tag, or merge-base) along two independent axes — Standards (documented coding standards and smell baseline) and Spec (originating requirements and scope).
---

# Code Review

Review code changes along two independent axes: **Standards** and **Spec**.

## Core Concept: Two Independent Axes

Reviews are evaluated along two separate axes without blending or cross-ranking:
1. **Standards**: Does the code follow repository coding standards, conventions, security guidelines, and baseline code smells?
2. **Spec**: Does the code implement what was requested? Are there missing requirements, incorrectly implemented requirements, or scope creep?

Why two axes? A change can pass Standards while failing Spec (clean code that builds the wrong thing), or pass Spec while failing Standards (correct functionality with sloppy implementation). Keeping them separate prevents one from masking the other.

## Workflow

1. **Identify Fixed Point**: Determine the base reference (`HEAD~1`, `main`, or specific commit/tag).
2. **Retrieve Diff & Context**:
   - Run `git diff <fixed-point>..HEAD` (or examine specified files).
   - Check repo standards (`CODING_STANDARDS.md`, `CONTRIBUTING.md`, `AGENTS.md`, architecture invariants).
   - Check spec/task instructions.
3. **Execute Standards Review**:
   - Hard violations of repo standards (citing rules/files).
   - Baseline code smells (naming, duplication, complexity, error handling, security invariants).
4. **Execute Spec Review**:
   - (a) Missing or partial requirements.
   - (b) Scope creep / unintended changes.
   - (c) Misimplemented requirements.
5. **Aggregate Report**:
   - Report under `## Standards` and `## Spec` headers.
   - Summarize total findings and worst issue per axis.
