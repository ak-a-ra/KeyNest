# Contributing to KeyNest 🤝

Thank you for your interest in contributing to **KeyNest**! KeyNest is an ultra-fast, encrypted API Key & Developer Secret Vault for Android built with Kotlin, Jetpack Compose, and Material Design 3.

We welcome contributions from everyone—whether you're fixing a bug, improving documentation, or adding a new feature from our [Roadmap](ROADMAP.md).

---

## 🔒 Security Invariants

KeyNest deals with sensitive developer credentials and cryptographic operations. Before submitting code, please ensure your changes uphold these strict security rules:

1. **Keystore-Backed Encryption**: Key values must always be encrypted at rest using AES-256 GCM backed by Android KeyStore (`EncryptedSharedPreferences`). Never store plaintext keys in SQLite, Room without encryption, standard SharedPreferences, or flat files.
2. **Masked Inputs**: Secret and API key input fields must remain masked by default (`PasswordVisualTransformation`) with an explicit reveal toggle.
3. **No Logging**: Never log key values, plain secrets, or PIN hashes—even in debug builds (`Log.d`, `println`, etc.).
4. **Sensitive Clipboard Flags**: All clipboard writes must preserve `ClipDescription.EXTRA_IS_SENSITIVE` on API 33+ (Android 13+).
5. **Offline & Privacy First**: The vault is local-first. Do not introduce unencrypted third-party analytics, remote logs, or mandatory cloud dependencies.

---

## 🛠️ Development Setup

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Ladybug / Meerkat or newer)
- JDK 17 or JDK 21
- Android SDK Platform 34+

### Building & Testing
Clone the repository and build via Gradle:

```bash
# Clone the repository
git clone https://github.com/ak-a-ra/KeyNest.git
cd KeyNest

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## 📐 Architecture & Code Style

- **Language**: 100% Kotlin.
- **UI Framework**: Jetpack Compose adhering strictly to Material Design 3 (M3).
- **Architecture**: MVVM with Clean Architecture principles (`ViewModel`, `StateFlow`, Coroutines).
- **Minimal Dependencies (YAGNI)**: Utilize standard library and native Android platform APIs before adding external dependencies.
- **Accessibility & Testability**:
  - Add `Modifier.testTag("unique_tag_name")` to interactive UI components.
  - Keep user-facing strings in `res/values/strings.xml`.

---

## 🔄 Contribution Workflow

1. **Find an Issue or Feature**: Check our [Roadmap](ROADMAP.md) or open an issue on [GitHub](https://github.com/ak-a-ra/KeyNest/issues).
2. **Fork & Branch**: Create a feature branch off `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Write Clean Code**: Keep commits atomic and focused.
4. **Verify Build**: Ensure code compiles without errors or test failures:
   ```bash
   ./gradlew assembleDebug test
   ```
5. **Submit a Pull Request**: Open a PR against `main` explaining your changes and referencing any related issues.

---

## 📜 License

By contributing to KeyNest, you agree that your contributions will be licensed under the project's [MIT License](LICENSE).
