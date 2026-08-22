<div align="center">

<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />

<h1>Built with AI Studio</h1>

<p>Build Gemini-powered apps from a prompt.</p>

<a href="https://aistudio.google.com/apps">Open AI Studio</a>

</div>

# KeyNest 🔐

KeyNest is an encrypted Android vault for API keys and developer secrets. It uses Jetpack Compose, Material 3, and Android Keystore-backed encryption.

🗺️ [Roadmap](ROADMAP.md) | 🤝 [Contributing](CONTRIBUTING.md) | 📄 [License](LICENSE)

---

## ⚡ What it does

- **Encrypted storage:** Secrets live in `EncryptedSharedPreferences`, protected by Android Keystore and AES-256 GCM. Plaintext never goes to disk.
- **Quick copy:** Copy a value with feedback and an automatic clipboard-clear timer. On Android 13 and later, copied secrets carry the `EXTRA_IS_SENSITIVE` flag.
- **Clipboard detection:** Recognizes common API key formats, including OpenAI, Gemini, Anthropic, Stripe, GitHub, and AWS keys, then offers to save them.
- **Search and filters:** Search from a floating bar, filter by category and environment, and reach security tools or theme settings from the navigation drawer.
- **Key checks:** Calculates Shannon entropy, assigns a strength score, and flags weak or expired keys.
- **Expiry tracking:** Shows fresh, expiring, and overdue keys with progress indicators and a quick rotation action.
- **Key generator:** Creates secure tokens, API keys, hex secrets, and UUIDs.
- **`.env` import and export:** Import configuration files in bulk or export saved values as a formatted `.env` file.
- **Encrypted vault backup & restore:** Export entire encrypted vault to portable `.keynest` files protected by PBKDF2 (100k rounds) + AES-256-GCM for cross-device migration and cold storage.
- **Optional PIN lock:** Lock the vault with a PIN when you need extra protection from someone looking over your shoulder.

---

## 🛠️ Stack

- Kotlin
- Jetpack Compose with Material Design 3
- Android Architecture Components: `ViewModel`, Kotlin Coroutines, and `StateFlow`
- AndroidX Security Crypto: `EncryptedSharedPreferences`, AES256-SIV, and AES256-GCM
- Cobalt and soft-periwinkle surfaces, with emerald and rose status colors

---

## 🔒 Security rules

1. **Encrypted storage only:** Persist secrets only in `EncryptedSharedPreferences` backed by Android's `MasterKey`. Do not use plain `SharedPreferences` or unencrypted databases.
2. **Masked fields:** Keep secret values masked by default. Revealing a value must be an explicit action.
3. **Sensitive clipboard:** On Android 13 and later, mark every secret copied to the clipboard with `ClipDescription.EXTRA_IS_SENSITIVE`.
4. **No secret logs:** Do not write API key values to Logcat or any debug log.
5. **No Android backups:** `android:allowBackup="false"` prevents secrets from being extracted through ADB backup.

---

## 🚀 Get started

### Requirements

- Android Studio Ladybug, Meerkat, or newer
- Android SDK 34 or newer
- JDK 17 or newer

### Build and run

1. Clone the repository.
2. Open it in Android Studio.
3. Build a debug APK and run it on a device or emulator:

   ```bash
   ./gradlew assembleDebug
   ```

### 🤖 CI/CD & Quality Gates

`.github/workflows/build-release.yml` builds a debug APK and publishes it as a GitHub release when you push a tag matching `v*`, such as `v1.0.0`. The APK is unsigned. For GitHub CLI authentication and release steps, see [docs/github-setup.md](docs/github-setup.md).

OpenCode responds to `/opencode` and `/oc` from repository owners, members, and collaborators in issue or pull-request comments. It also reviews same-repository pull requests that target `main`.

#### 🛡️ Local Validation Gate (`no-mistakes`)

KeyNest is configured with [`no-mistakes`](https://github.com/kunchenguid/no-mistakes) via `.no-mistakes.yaml` for pre-push validation (unit tests, linting, code review, documentation sync):

- Run pipeline manually: `no-mistakes axi run --intent "<goal>"`
- Direct git push proxy: `git push no-mistakes`
- Inspect gate status: `no-mistakes axi status`

---

## 📄 License

KeyNest is released under the [MIT License](LICENSE).
