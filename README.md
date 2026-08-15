<div align="center">

<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />

  <h1>Built with AI Studio</h2>

  <p>The fastest path from prompt to production with Gemini.</p>

  <a href="https://aistudio.google.com/apps">Start building</a>

</div>

# KeyNest 🔐

> Ultra-fast, encrypted API key and developer secret vault for Android. Built with Jetpack Compose, Material 3, and Android Keystore encryption.

🗺️ **[Roadmap](ROADMAP.md)** | 🤝 **[Contributing](CONTRIBUTING.md)** | 📄 **[License](LICENSE)**

---

## ⚡ Highlights

- **🔒 Hardware-Backed Security**: Encrypted using Android Keystore and AES-256 GCM (`EncryptedSharedPreferences`). No plaintext secrets on disk.
- **⚡ 1-Tap Tactile Copy**: Instant copy with visual feedback, auto-clearing clipboard timers, and API 33+ `EXTRA_IS_SENSITIVE` flag protection.
- **📋 Smart Clipboard Sniffer**: Automatically detects known API key formats (OpenAI, Gemini, Anthropic, Stripe, GitHub, AWS, etc.) from clipboard and prompts to securely vault them.
- **🗂️ Rich Metadata & Navigation Drawer**: Google Keep-style floating search bar with Modal Navigation Drawer layout for filtering by Category (`AI & LLMs`, `Cloud`, `Payments`), Environment (`Production`, `Staging`, `Dev`), and quick access to Security Tools & Theme toggles.
- **🛡️ Security & Entropy Auditing**: Real-time Shannon entropy calculation and strength scoring, identifying weak or expired keys.
- **⏳ Key Expiration & Rotation Tracker**: Material 3 visual cards with 3-tier color-coded alerts (🟢 Fresh, 🟠 Approaching Expiry, 🔴 Overdue), animated lifecycle progress indicators, and 1-tap quick rotation CTA.
- **🎲 Built-in Key & Secret Generator**: Generate cryptographically secure tokens, API keys, hex secrets, and UUIDs on demand.
- **📄 Batch `.env` Import / Export**: One-tap export to formatted `.env` files or bulk import secrets from existing config files.
- **🔢 Master PIN & Lock**: Optional Vault lock screen with PIN protection to safeguard secrets from shoulder surfers.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **State Management**: Android Architecture Components (`ViewModel`, Kotlin Coroutines, `StateFlow`)
- **Encryption**: AndroidX Security Crypto (`EncryptedSharedPreferences`, AES256-SIV / AES256-GCM)
- **Theming**: Custom Vibrant Palette (Cobalt & Soft Periwinkle surfaces, Emerald/Rose status accents)

---

## 🔒 Security Invariants

1. **Storage Isolation**: Secrets are only ever persisted using `EncryptedSharedPreferences` backed by the Android MasterKey system. Plain `SharedPreferences` or unencrypted databases are strictly prohibited.
2. **Masked by Default**: All sensitive fields in input dialogs and cards render masked values (`••••••••`) by default with an explicit reveal toggle.
3. **Clipboard Hygiene**: Clipboard writes are flagged with `ClipDescription.EXTRA_IS_SENSITIVE` on Android 13+ (API 33+) to prevent clipboard managers from exposing secrets.
4. **No Logging**: Sensitive API key values are never emitted to Logcat or debug logs.
5. **Backup Disabled**: `android:allowBackup="false"` is enforced to prevent secrets extraction via ADB backup.

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat or newer
- Android SDK 34+
- JDK 17+

### Building and Running
1. Clone this repository.
2. Open the project in Android Studio.
3. Run the application on an Android device or emulator:
   ```bash
   ./gradlew assembleDebug
   ```

### 🤖 CI/CD Build & Release
This repository comes with an automated **GitHub Actions** workflow (`.github/workflows/build-release.yml`) that builds and releases KeyNest debug APKs:
- **Trigger**: Pushing a new git tag matching the `v*` pattern (e.g., `git tag v1.0.0 && git push origin v1.0.0`).
- **Build Output**: Compiles an unaligned/unsigned debug APK.
- **Release**: Automatically creates a GitHub Release named after the pushed tag, attaches the compiled `.apk` as a release asset, and publishes it immediately.
- **CLI Setup Guide**: See [docs/github-setup.md](docs/github-setup.md) for step-by-step GitHub CLI (`gh`) authentication and release instructions.

---

## 📄 License
MIT License. See [LICENSE](LICENSE) for details.
