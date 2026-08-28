# AGENTS.md — Core Files Subsystem

## Scope

Governs `com.example.core.files`: `VaultFileManager.kt`.

## Responsibilities

- Android Storage Access Framework (SAF) URI read/write, file pickers, export stream handling, and MIME type management.

## Invariants

- File I/O operations must run exclusively on `Dispatchers.IO`.
- Streams must be safely flushed and closed after export/import operations.
