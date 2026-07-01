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

## Spotify integration (`releases/spotify-integration/`)

Optional background music via the Spotify app (App Remote SDK). Bundled `game-music.ogg` still plays when Spotify is unavailable.

### Spotify Developer Dashboard setup

1. Create an app at [developer.spotify.com/dashboard](https://developer.spotify.com/dashboard).
2. Select **Android** as the API you plan to use.
3. **Redirect URI** — enter exactly:

   `com.aocc.majorproject://callback`

4. **Android package**: `com.aocc.majorproject`
5. **SHA-1 fingerprint** (debug build on this machine):

   `35:BA:10:CA:3C:13:F4:33:4F:84:07:47:A8:9C:63:8E:9D:49:BE:5E`

   For release builds, add your release keystore SHA-1 as well.

6. Copy the **Client ID** into `majorProject/src/main/res/values/strings.xml`, replacing `YOUR_SPOTIFY_CLIENT_ID`.

### Requirements on device

- Spotify app installed and signed in
- **Spotify Premium** (required to play a specific playlist URI)
- Music toggle enabled in-game (same button as before)

### How it works

- On launch, the game tries to connect to Spotify in the background.
- If connected, the music button controls Spotify playback of the playlist in `spotify_playlist_uri` (default: Spotify’s “Feel Good Indie” playlist).
- If Spotify is not configured, not installed, or connection fails, bundled OGG music is used instead.
- First connect may show Spotify’s authorization screen (built-in App Remote auth).

### Branch

`cursor/spotify-integration-a6be`
