# SDD-Major-Project-Game

High-school Android game with Google Play Games integration. First exposure to object-oriented code!

## Modern Android builds

This project is being upgraded from its original API 22 codebase in incremental phases. See [MODERNIZATION.md](MODERNIZATION.md) for details and test plans.

**Latest debug APK:** [`releases/latest/MajorProject-debug.apk`](releases/latest/MajorProject-debug.apk) (updated in place when a new build is shared).

Quick build:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew :majorProject:assembleDebug
```

APK output: `majorProject/build/outputs/apk/debug/majorProject-debug.apk`

Copy to `releases/latest/MajorProject-debug.apk` when sharing a test build.

### Install troubleshooting

If Android reports **“App not installed as package appears to be invalid”**:

1. **Uninstall the existing app first** — Settings → Apps → MajorProject → Uninstall.  
   Sideloaded builds share `com.aocc.majorproject`; Android rejects a new APK if the signing certificate does not match the installed copy (common when switching between local builds, older phase APKs, or the Spotify branch).
2. **Verify the download** — `MajorProject-debug.apk` should be about **8.0 MB**. A much smaller file is usually a failed or partial download; re-download on Wi‑Fi.
3. **Install from a file manager**, not an in-browser preview. After download completes, open the APK from Files/Downloads.
4. **Optional (ADB):** `adb install -r releases/latest/MajorProject-debug.apk`

All shared debug APKs are signed with the committed `majorProject/debug.keystore` so upgrades between agent builds work without uninstalling (once you are on this signing key).
