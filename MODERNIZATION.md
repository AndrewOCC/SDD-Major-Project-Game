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
- Version label shows **v1.2.2** in the bottom-right corner.

### Branch

`cursor/framerate-viewport-a6be`

### Test APK

Always use **`releases/latest/MajorProject-debug.apk`** — this file is overwritten when a new build is shared.

## Phase 5 — Game session state (`cursor/game-session-a6be`)

**What changed**
- Introduced `GameSession` to hold score, speed, spawn timers, and game-over flag per play-through.
- Removed static fields from `GameScreen`; `Player`, `Enemy`, `PowerUp`, and `EnemyController` receive session context explicitly.
- Fixed score upload guard (`==` instead of accidental assignment).

**What to verify**
- Retry starts a clean run (score/combo reset, no stale game-over state).
- Game over still submits score once.
- Unit tests pass without static `GameScreen` setters.

### Branch

`cursor/game-session-a6be`

## Phase 7 — Async asset loading (`cursor/phase7-async-ci-a6be`)

**What changed**
- Added `AssetLoader` to decode images, sounds, and fonts on a background thread.
- `LoadingScreen` shows splash art, a progress bar, and percentage while assets load.
- Music loads on the main thread after background work completes (MediaPlayer requirement).
- Removed synchronous load-all-in-one-frame behaviour from the old loading screens.

**What to verify**
- Splash appears immediately, then progress advances smoothly to 100%.
- Main menu opens without a long frozen frame on first launch.
- Version shows **v1.4.0**.

### Branch

`cursor/phase7-async-ci-a6be`

## Phase 9 — CI and SDK 35

**What changed**
- GitHub Actions workflow (`.github/workflows/ci.yml`) runs unit tests and assembles debug on every push/PR.
- `compileSdk` and `targetSdk` bumped to **35**.

**What to verify**
- CI passes on GitHub for this branch.
- App installs and runs on Android 14+ devices.

## Phase 10 — Anchor-based UI layout (`cursor/ui-layout-a6be`)

**What changed**
- `UiLayout` / `MainMenuLayout` — menu buttons and GPG icons anchored to the 1280×720 world rectangle (centred play/tutorial row, right-aligned GPG icons).
- `SettingsPanel` — centred container composing a **Sound** column (icons with gap) and **Tilt Options** column for ready/pause screens.
- Touch targets now match layout regions on ultrawide/letterboxed displays.

**What to verify**
- Play and Tutorial taps register on ultrawide devices (AYN Thor).
- Pause/ready settings panel is centred with sound icons spaced apart.
- Version shows **v1.5.0**.

## Phase 11 — Jetpack Compose menus (`cursor/ui-layout-a6be`)

**What changed**
- Hybrid layout: canvas game loop with a transparent Jetpack Compose overlay for menus and settings.
- Ready/pause settings use a Compose panel with sound toggles and tilt options in a proper column layout; the tap-to-start/resume prompt sits **below** the panel (no overlap).
- Main menu Play, Tutorial, sign-in, and Play Games shortcuts are Compose buttons over the canvas background art.
- Shared preference logic moved to `GameSettings`; canvas `SettingsPanel` removed.

**What to verify**
- Settings panel no longer overlaps "Press anywhere to start/resume".
- All three tilt options fit inside the panel on ultrawide devices.
- Main menu buttons respond on AYN Thor and other landscape devices.
- Version shows **v1.6.0**.

**v1.6.2 update:** Main menu navigation restored to canvas touch regions (Play/Tutorial/GPG/sign-in) so taps align with background art. Compose overlay is settings-only. Viewport syncs from root layout; settings overlay letterbox computed from overlay size in pixels.

### Branch

`cursor/ui-layout-a6be`

## Phase 12 — Native resolution rendering (`cursor/native-resolution-a6be`)

**What changed**
- Removed the fixed 1280×720 off-screen framebuffer; frames render directly to the device surface at native pixel resolution.
- `AndroidGraphics` applies the viewport transform (`translate` + `scale`) so all game code keeps using world coordinates (1280×720).
- Assets draw at world sizes and scale up crisply on high-DPI displays (`FILTER_BITMAP_FLAG`).
- `drawARGB` overlays now cover the world rectangle only (correct with scaled canvas).
- Game logic, UI layout, and touch mapping unchanged — still resolution-independent.

**What to verify**
- Graphics look sharp on high-resolution handhelds (not soft 720p upscale).
- Touch targets still align on ultrawide letterboxed displays.
- Version shows **v1.7.0**.

### Branch

`cursor/native-resolution-a6be`

## Phase 12b — 1× / 2× asset tiers (`cursor/native-resolution-a6be`)

**What changed**
- `AssetScale` picks **1×** or **2×** bitmaps at load time from viewport scale (threshold 1.5×).
- High-density art lives in `assets/2x/` with the same filenames as 1×; falls back to 1× if missing.
- `AndroidImage` tracks pixel scale; `drawImage` maps 2× bitmaps into the same world layout size (pixels ÷ 2).
- Initial `2x/` variants generated for all game PNGs (UI icons, backgrounds, splash, tutorial).

**Adding sharper art later**
- Replace files under `majorProject/src/main/assets/2x/` — no code changes needed.
- Devices with viewport scale ≥ 1.5 load 2× automatically; others stay on 1×.

**What to verify**
- Version shows **v1.7.1**
- UI sprites look sharper on high-DPI handhelds (AYN Thor)
- Layout unchanged (same world-coordinate sizes)
