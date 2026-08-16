# Contributing to KeyNest 🤝

Thanks for contributing to KeyNest, an Android vault for API keys and developer secrets. Bug fixes, documentation corrections, and focused features from the [roadmap](ROADMAP.md) are all welcome.

---

## 🔒 Security rules

KeyNest stores credentials, so security rules apply to every change:

1. **Encrypt saved values:** Store key values with Android Keystore-backed `EncryptedSharedPreferences` and AES-256 GCM. Never put plaintext secrets in SQLite, unencrypted Room tables, standard `SharedPreferences`, or files.
2. **Mask secrets by default:** API key and secret fields use `PasswordVisualTransformation`. Users must choose to reveal a value.
3. **Do not log secrets:** Do not log key values, plaintext secrets, or PIN hashes, including in debug builds. That includes `Log.d` and `println`.
4. **Mark clipboard data as sensitive:** On Android 13 and later, preserve `ClipDescription.EXTRA_IS_SENSITIVE` for every clipboard write.
5. **Keep the vault local:** Do not add unencrypted analytics, remote logging, or required cloud services.

---

## 🛠️ Set up the project

### Requirements

- [Android Studio](https://developer.android.com/studio), Ladybug, Meerkat, or newer
- JDK 17 or 21
- Android SDK Platform 34 or newer

### Build and test

```bash
# Clone the repository
git clone https://github.com/ak-a-ra/KeyNest.git
cd KeyNest

# Build a debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

---

## 📐 Code style

- Write Kotlin.
- Use Jetpack Compose and Material Design 3.
- Keep the existing MVVM structure: `ViewModel`, `StateFlow`, and coroutines.
- Prefer Kotlin, Android, and existing dependencies before adding a library.
- Give interactive UI elements a unique `Modifier.testTag("...")`.
- Put user-visible strings in `res/values/strings.xml`.

---

## 🔄 Submit a change

1. Find an issue, pick an item from the [roadmap](ROADMAP.md), or open an issue first.
2. Create a branch from `main`:

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. Keep commits focused.
4. Before opening a pull request, build and test the app:

   ```bash
   ./gradlew assembleDebug test
   ```

5. Open a pull request against `main`. Explain the change and link related issues.

---

## 📜 License

Contributions are released under the project's [MIT License](LICENSE).
