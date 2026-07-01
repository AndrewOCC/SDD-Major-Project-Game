# Android Modernization Plan

This game was originally built for Android API 22 (2015). It is being updated in small, testable steps. Each phase produces a debug APK you can sideload and verify before moving on.

## How to install

1. Enable **Install unknown apps** for your file manager or browser.
2. Copy the APK for the phase you want to test to your phone.
3. Open it and install. Only one build needs to be installed at a time (same `applicationId`).

## Phase 1 — Gradle baseline (`releases/phase1-gradle-baseline/`)

**What changed**
- Restored the missing Gradle build (wrapper, `settings.gradle`, module `build.gradle` files).
- Upgraded toolchain: AGP 8.5, Gradle 8.7, compile/target SDK 34, min SDK 21.
- Migrated `baseGameUtils` to AndroidX `FragmentActivity`.
- Added `android:exported="true"` on the launcher activity (required on Android 12+).

**What to verify**
- App installs and launches.
- Splash screen → main menu → gameplay all work.
- Touch controls, sound, and landscape orientation behave as before.
- Google Play Games sign-in may fail (legacy SDK); that is expected until Phase 4.

## Phase 2 — Manifest & permissions (`releases/phase2-manifest-permissions/`)

**What changed**
- Removed deprecated manifest `package` / `uses-sdk` entries (now controlled by Gradle).
- Removed unused `WRITE_EXTERNAL_STORAGE` permission.
- Switched file I/O fallback from external storage to app-internal storage.

**What to verify**
- No storage permission prompt on install or launch.
- Game still saves/loads preferences correctly.
- Everything from Phase 1 still works.

## Phase 3 — Modern lifecycle & display (`releases/phase3-modern-apis/`)

**What changed**
- Replaced deprecated `onBackPressed()` with `OnBackPressedDispatcher`.
- Updated display sizing/rotation to modern APIs (no deprecated `Display.getWidth()`).
- Enabled edge-to-edge layout with immersive fullscreen on current Android versions.

**What to verify**
- Back button returns through menus / exits as before.
- Game fills the screen on notched and gesture-nav devices (no odd letterboxing).
- Rotation/orientation handling still correct in landscape.

## Phase 4 — Play Games Services v2 (`releases/phase4-play-games-v2/`)

**What changed**
- Removed vendored 2014 `google-play-services.jar` library module.
- Integrated Play Games Services v2 (`play-services-games-v2`).
- Rewrote sign-in, leaderboards, achievements, and score submission.

**What to verify**
- Google Play Games sign-in flow opens.
- Leaderboards and achievements UI launch when signed in.
- Scores and achievements sync (requires a Google account linked to the Play Console app).

## Branches

| Phase | Branch |
|-------|--------|
| 1 | `cursor/phase1-gradle-baseline-a6be` |
| 2 | `cursor/phase2-manifest-permissions-a6be` |
| 3 | `cursor/phase3-modern-apis-a6be` |
| 4 | `cursor/phase4-play-games-v2-a6be` |

Each branch builds with:

```bash
export ANDROID_HOME=$HOME/Android/Sdk   # or your SDK path
./gradlew :majorProject:assembleDebug
```

APK output: `majorProject/build/outputs/apk/debug/majorProject-debug.apk`
