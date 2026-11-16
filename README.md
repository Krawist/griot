# Griot

Griot is an Android songwriting app where AI is a co-pilot, never the author. It counts syllables and colours rhymes as you write, in French and English, keeps every version of every verse, and syncs your songs to Google Drive so a text is never lost. The AI suggests rhymes, rephrases or critiques a verse next to your text; the song stays yours.

The context for Claude Code is in [`CLAUDE.md`](CLAUDE.md), which also links the product documents.

## Requirements

- A recent Android Studio (the same version that builds Cauri), JDK 17
- An Android 10+ phone

## Getting started

Open the folder in Android Studio and let it sync, or from a terminal:

```bash
./gradlew assembleDebug        # debug APK: app/build/outputs/apk/debug/
./gradlew installDebug         # installs on the connected phone
```

Checks run by CI on every pull request:

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```

Fix formatting with `./gradlew ktlintFormat`.

## First sync

The build setup (Gradle wrapper, convention plugins, dependency versions) is copied from Cauri, which builds on this machine, but this skeleton has not been compiled yet. If the first sync fails on a missing or incompatible version, adjust `gradle/libs.versions.toml`: Android Studio suggests the available versions.

## Signing

- **debug**: Android Studio's local debug key, `applicationId` suffixed with `.debug`, so both variants can be installed side by side.
- **release**: copy `keystore.properties.example` to `keystore.properties` and fill it in. This file and the `.jks` files are ignored by git. Without it, the release build is unsigned.

Create the key once:

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/griot-release.jks -alias griot -keyalg RSA -keysize 4096 -validity 10000
```

## Structure

```
app/                    application, navigation between features
feature/onboarding      welcome, name, language, sync, import
feature/library         library of songs
feature/editor          structured editor, AI panel, version history
feature/ideas           ideas box
feature/settings        profile, sync, AI, texts
core/design             Compose theme (light and dark) and components
core/data               DataStore preferences, Room database
core/phonetics          syllables, rhymes and paste splitting, French and English (pure Kotlin)
core/sync               sync repository and Google Drive
core/ai                 AI providers, on-device and online
build-logic/            Gradle convention plugins
config/                 detekt rules
docs/                   MVP tickets
.github/workflows/      CI workflow
```
