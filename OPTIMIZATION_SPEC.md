# KeyNest Optimization Spec

This implementation specification outlines the path to optimizing the KeyNest Android app across four key pillars: **Security**, **Performance**, **UI/UX**, and **Build/Distribution**. Following the Ponytail "YAGNI-first" philosophy, these are high-impact, native-first optimizations that require minimal new dependencies.

## 1. 🔐 Security & Privacy Optimizations (Crucial)

As a secrets vault, protecting data in memory and on-screen is paramount.

- **[ ] 1.1 Screen Capture Prevention (`FLAG_SECURE`)**
  - **Action**: Add `window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)` in `MainActivity.onCreate()`.
  - **Impact**: Prevents users (and background apps) from taking screenshots or recording the screen while the vault is visible. Also blanks out the preview in the Android Recents app switcher.

- **[ ] 1.2 Automatic Background Locking**
  - **Action**: Integrate `androidx.lifecycle:lifecycle-process` to detect when the app goes into the background (`ProcessLifecycleOwner.get().lifecycle.addObserver`).
  - **Impact**: Automatically sets `isVaultLocked = true` when the user leaves the app for more than 10 seconds, preventing unauthorized access if the phone is handed over unlocked.

- **[ ] 1.3 Strict Clipboard Expiration**
  - **Action**: Set a timeout using `ClipData.Item.setExpirationTime` (or clear the clipboard automatically after 60 seconds using `WorkManager` or Coroutines).
  - **Impact**: Prevents sensitive keys from lingering in the system clipboard indefinitely.

## 2. ⚡ Performance & Search Optimizations

The goal is to maintain the "Ultra-fast" promise, ensuring zero-latency queries.

- **[ ] 2.1 Full-Text Search (FTS4) for SQLite**
  - **Action**: Currently, `ApiKeyDao.searchKeys` uses `LIKE '%query%'`, which triggers a full table scan. We will create an `@Fts4` companion entity in Room (`ApiKeyFts`) and use the `MATCH` operator.
  - **Impact**: Drops search latency from O(N) to O(1) index lookups, enabling true real-time filtering even with thousands of keys.

- **[ ] 2.2 Baseline Profiles for Startup Time**
  - **Action**: Add a `:macrobenchmark` module to generate a `baseline-prof.txt` file executing the Critical User Journeys (CUJ): starting the app, entering the PIN, and scrolling the vault grid.
  - **Impact**: Up to 30% faster app startup time and smoother first-frame renders due to AOT (Ahead-of-Time) compilation by Android's ART.

- **[ ] 2.3 Compose State Flow Distinctness**
  - **Action**: Apply `.distinctUntilChanged()` to all Room `Flow` emissions in `VaultViewModel`.
  - **Impact**: Prevents cascading UI recompositions when unrelated database fields (like `lastCopiedAt`) are updated.

## 3. 🎨 UI/UX & Compose Recomposition Optimizations

Eliminating UI jank and skipping unnecessary frame calculations.

- **[ ] 3.1 Stable & Immutable State Markers**
  - **Action**: Add the `kotlinx-collections-immutable` dependency. Replace `List<T>` with `ImmutableList<T>` in ViewModels and UI state classes. Annotate domain models like `ApiKeyItem` with `@Stable` or `@Immutable`.
  - **Impact**: Jetpack Compose can securely skip recomposing unchanged API Key cards during scrolling.

- **[ ] 3.2 Derived State for Scroll Observations**
  - **Action**: Wrap list scroll position reads in `derivedStateOf { listState.firstVisibleItemIndex > 0 }` (e.g., for showing/hiding the top app bar shadow or FAB).
  - **Impact**: Prevents recomposing the entire parent layout on every pixel scrolled.

## 4. 📦 Build & APK Size Optimizations

Keeping the binary small and secure.

- **[ ] 4.1 R8 Code Shrinking & Obfuscation**
  - **Action**: Ensure `isMinifyEnabled = true` and `isShrinkResources = true` in `app/build.gradle.kts` for the `release` block. Configure `proguard-rules.pro` to keep Room schemas.
  - **Impact**: Drastically reduces APK size, speeds up download times, and obfuscates code to prevent reverse engineering of the security implementations.

- **[ ] 4.2 Resource Configuration Splits**
  - **Action**: Add `resourceConfigurations += setOf("en")` (and any supported languages) to the default config.
  - **Impact**: Strips out unused Android framework localization strings (e.g., from `androidx.appcompat` or `material3`) for languages the app doesn't actually support.

---
**Next Steps**: 
Review this specification. Let me know which category (Security, Performance, UI, or Build) you would like to implement first, and I will execute the changes.
