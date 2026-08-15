# Contributing to KeyNest

Thank you for your interest in contributing to **KeyNest**! KeyNest is an ultra-fast, encrypted API Key & Developer Secret Vault for Android built with Kotlin, Jetpack Compose, and Material Design 3.

---

## 🔒 Security First

KeyNest deals with sensitive developer credentials and cryptographic operations. Before writing code, please review these strict security invariants:

1. **Keystore-Backed Encryption**: Key values must always be encrypted at rest using AES-256 GCM backed by the Android KeyStore (`Cryptography.kt` / `EncryptedSharedPreferences`). Never store plaintext keys in SQLite, Room without encryption, shared preferences, or flat files.
2. **Masked Inputs**: Secret and API key input fields must stay masked by default (`PasswordVisualTransformation`) with an explicit reveal toggle.
3. **No Logging**: Never log key values, plain secrets, or PIN hashes, even in debug builds (`Log.d`, `println`, etc.).
4. **Sensitive Clipboard Flags**: All clipboard writes must preserve `ClipDescription.EXTRA_IS_SENSITIVE` on API 33+.
5. **No Cloud Sync Without End-to-End Encryption**: The vault is local-first. Do not introduce unencrypted third-party analytics, remote logs, or cloud tracking.

---

## 🛠️ Development Setup

### Prerequisites
- Android Studio Ladybug (or newer) / IntelliJ IDEA with Android SDK
- JDK 17 or JDK 21
- Android SDK Platform 34+ / Build Tools

### Building the Project
Clone the repository and build using Gradle:

```bash
# Clone the repository
git clone https://github.com/your-username/KeyNest.git
cd KeyNest

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## 📐 Code Style & Architecture Guidelines

- **Language**: Kotlin exclusively for UI and logic.
- **UI Framework**: Jetpack Compose adhering strictly to Material Design 3 (M3).
- **Architecture**: MVVM with Clean Architecture principles.
- **YAGNI & Minimal Code**: Reach for stdlib and native Android platform features before pulling in external dependencies.
- **Naming & Testing**:
  - Add `Modifier.testTag("unique_tag_name")` to interactive elements and buttons.
  - Follow Android resource conventions (`res/values/strings.xml` for user-facing copy).

---

## 🔄 Contribution Workflow

1. **Fork & Branch**: Create a feature branch from `main`:
   ```bash
   git checkout -b feature/grid-view-toggle
   ```
2. **Implement**: Keep commits atomic, well-described, and focused on a single responsibility.
3. **Verify Build**: Ensure code compiles without errors or lint warnings:
   ```bash
   ./gradlew assembleDebug test
   ```
4. **Submit PR**: Open a Pull Request against `main` with a clear description of the feature or bug fix and reference any relevant issues or roadmap items.

---

## 📜 License

By contributing to KeyNest, you agree that your contributions will be licensed under the project's [Apache 2.0 / MIT License](LICENSE).
