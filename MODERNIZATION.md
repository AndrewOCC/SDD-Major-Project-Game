# Android Modernization Plan

This game was originally built for Android API 22 (2015). It is being updated in small, testable steps. Each phase produces a debug APK you can sideload and verify before moving on.

## How to install

1. Enable **Install unknown apps** for your file manager or browser.
2. Download the current debug build from **`releases/latest/MajorProject-debug.apk`** (updated in place on each share).
3. Open it and install. Only one build needs to be installed at a time (same `applicationId`).

Historical phase APKs remain under `releases/phase*/` for regression comparison only.

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

## Unit tests

Run the JVM unit test suite (Robolectric + JUnit 4):

```bash
./gradlew :majorProject:testDebugUnitTest
```

Coverage includes:

| Test class | What it verifies |
|------------|------------------|
| `PersonalMethodsTest` | Value clamping, touch hit boxes, rectangle collision |
| `PoolTest` | Object pool reuse and max-size cap |
| `ButtonTest` | Menu and play button touch targets |
| `PlayerTest` | Arena boundary clamping, shield drain, health/game-over |
| `EnemyControllerTest` | Enemy spawn timing and list management |

## Debugging crashes without ADB

If the app crashes, relaunch it. A dialog may show the previous crash summary.

The full log is written to app-internal storage:

`Android/data/com.aocc.majorproject/files/crash_log.txt`

On your device, open a file manager, browse to that path (or `Internal storage/Android/data/com.aocc.majorproject/files/`), and copy `crash_log.txt` to share for debugging.

Common causes fixed in recent builds:

- Missing background music file (`darude-sandstorm.m4a`) — game now continues silently without music
- Google Play Games unavailable on device — game continues without Play Games features
- Zero-size display metrics on some handhelds — scaling now guarded

## Phase 8 — Framerate & viewport independence

**What changed**
- Game simulation now uses elapsed **seconds** (`deltaSeconds`) instead of assuming 60 FPS frame steps.
- Player, enemies, power-ups, spawn timers, and difficulty ramps scale with `secondsToSteps()` so gameplay feels the same at 60/90/120 Hz.
- Render loop uses **Choreographer** (VSYNC-aligned) instead of a busy-wait thread.
- Added `Viewport` letterboxing: 1280×720 world is scaled uniformly with black bars instead of stretched.
- Touch input uses the same uniform viewport transform as rendering.
- Added `com.aocc.majorproject.ui` helpers (`UiButton`, `ScoreBar`, `UiBanner`, `UiPanel`, `UiText`) so menu, score, and game-over text render centred in their bounds.

**What to verify**
- Game speed feels the same on a 60 Hz phone and a 120 Hz handheld (AYN Thor).
- Circles stay circular on ultrawide screens (no horizontal squash).
- Touch targets still line up with buttons after letterboxing.
- Menu, Retry, score bar, and game-over text are centred (not stuck on the left).
- Version label shows **v1.2.0** in the bottom-right corner.

### Branch

`cursor/framerate-viewport-a6be`

### Test APK

Always use **`releases/latest/MajorProject-debug.apk`** — this file is overwritten when a new build is shared.
