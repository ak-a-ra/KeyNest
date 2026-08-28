# AGENTS.md — Test Suites

## Scope

Governs `app/src/test/`: Robolectric JVM unit tests, database migration tests, cryptographic seam tests, and Roborazzi screenshot tests.

## Responsibilities

- Verifying repository behavior, Room database migrations (`AppDatabaseMigrationTest`), backup crypto, and screen UI snapshot contracts.

## Invariants

- All tests must pass cleanly via `./gradlew testDebugUnitTest` (`compile_applet`).
- Never delete or disable existing test cases.
