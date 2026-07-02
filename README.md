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
