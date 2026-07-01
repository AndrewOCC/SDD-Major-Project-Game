# SDD-Major-Project-Game

High-school Android game with Google Play Games integration. First exposure to object-oriented code!

## Modern Android builds

This project is being upgraded from its original API 22 codebase in incremental phases. See [MODERNIZATION.md](MODERNIZATION.md) for details, test plans, and debug APKs under `releases/`.

Quick build:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
./gradlew :majorProject:assembleDebug
```

Latest recommended build: branch `cursor/phase4-play-games-v2-a6be`.
