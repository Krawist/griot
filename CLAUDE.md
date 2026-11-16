# CLAUDE.md — Griot

Context for Claude Code. Read this file in full before any task, then the reference document relevant to the
current ticket.

## The product in short

Griot is an Android songwriting app where AI is a co-pilot, never the author. It is first a personal tool for its
author, who writes in French and/or English and once lost all his texts; it stays open to a public release later.

Three promises guide every choice: never lose a text, make the music of the words visible (syllables, rhymes,
structure), and offer smart help that respects the author's voice. The AI suggests rhymes, rephrases or critiques
a verse next to the text; it never writes the song in the author's place.

## Reference documents

The documents kept in Claude are the reference; they are written in French.

| Document                                   | Local copy        | Reference                                         |
|--------------------------------------------|-------------------|---------------------------------------------------|
| Vision and features (what and why)         | —                 | https://claude.ai/artifact/TFJQahYirQ72jEVJMs1Xoy |
| MVP tickets (GRI-01 to GRI-44, 3 phases)   | `docs/tickets.md` | —                                                 |
| Logo and mockups, light and dark (canvas)  | —                 | https://claude.ai/artifact/GtB94FRDSFC6mVmKKVfCfq |

`docs/tickets.md` is the detailed English working copy: acceptance criteria, dependencies and the mockup of each
screen, named by its artboard title on the canvas.

Before implementing a screen, look at its mockup. Song titles, verses and names in the mockups are examples.

## Decisions taken (do not question them unless asked)

- **Local-first.** Everything is written to the phone first and works offline. Google Drive (`appDataFolder` scope
  only) is the sync target, behind a `SyncRepository` interface so a Micronaut backend can replace it later. No
  backend and no proxy for now: the app is personal.
- **Never lose a text.** Every change is saved immediately and versioned. A sync conflict keeps both versions; nothing
  is ever overwritten. Everything can be exported as Markdown.
- **Phonetics are deterministic.** Syllables and rhymes come from code: Lexique for French, the CMU Pronouncing
  Dictionary for English, grapheme rules for unknown words, behind one `PhoneticEngine` interface. An LLM never
  counts syllables or decides a rhyme.
- **The AI is a co-pilot.** Suggestions are shown next to the text, never in its place. Every suggestion returned by a
  model is checked by the phonetic engine before display.
- **Hybrid AI** behind an `AiProvider` interface: Gemini Nano on the device (ML Kit GenAI Prompt API) when
  available, a small low-cost cloud model otherwise. Free tiers only during development, since some may train on the
  texts sent. The API key stays on the device (Android Keystore) while the app is personal.
- **French and English from the start**, for the interface and the writing. Each song has a writing language
  (French, English or automatic); in automatic mode the dominant language of the song decides.
- **A verse is one line of the model.** Enter creates a new verse; a long verse wraps on screen and stays one verse.
  A pasted text is split into sections from blank lines and section labels.

## Stack and conventions

- Kotlin, Jetpack Compose, Material 3 under the Griot theme. minSdk 29 (Android 10), compile 37, target 36. JDK 17.
- UDF architecture: `@HiltViewModel` + state in `StateFlow`, coroutines. Hilt (KSP), Room, DataStore, WorkManager
  and type-safe Navigation Compose (`@Serializable` routes).
- Navigation: each feature exposes its graph object (`LibraryGraph`, `EditorGraph`…) and a
  `NavGraphBuilder.xxxGraph(...)` extension; `:app` (`GriotApp.kt`) wires them. Features never navigate to each
  other directly: they take callbacks.
- Gradle Kotlin DSL, `gradle/libs.versions.toml` catalog, convention plugins in `build-logic/`:
  `griot.android.application`, `griot.android.library`, `griot.android.compose`, `griot.android.feature` (library +
  compose + Hilt + navigation + `:core:design`), `griot.android.hilt`, `griot.android.room` (schemas in `schemas/`),
  `griot.jvm.library` (pure Kotlin).
- Kotlin sources in `src/main/kotlin` and `src/test/kotlin`. Type-safe module accessors:
  `implementation(projects.core.design)`.
- Pure logic (phonetic engines, paste splitter, rhyme letters, conflict merge) in Kotlin without Android
  dependencies whenever possible, covered by JVM tests.
- User-facing texts: `strings.xml` in French (default) **and** English (`values-en`). Never hard-coded text in a
  composable (detekt `HardcodedUiText` checks it).
- Colours come from `GriotTheme.palette`, never raw values in screens. Magenta is the brand and main actions, orange
  is reserved for AI, turquoise for sync and states. Rhyme colours are their own set.
- Motion: every screen and state change is animated with the shared motion tokens of `:core:design` (GRI-44), from
  the first screens.
- Tests: JUnit 5 + Kotest assertions. Names start with `should`, one `@Nested inner class` per tested method,
  `// given`, `// when`, `// then`, `@ParameterizedTest` for variations. Fakes for repositories.
- Quality: `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` must pass before a ticket is done.
- Everything in English: code, identifiers, comments, KDoc, commit messages and documentation. Only the user-facing
  French strings stay in French.

### Modules

| Module                 | Content                                                        | Tickets           |
|------------------------|----------------------------------------------------------------|-------------------|
| `:app`                 | Application, navigation between features, signing              | 01, 03, 04        |
| `:feature:onboarding`  | Welcome, name, language, sync, import                          | 07–11             |
| `:feature:library`     | Library of songs, search, filters, trash                       | 12, 13            |
| `:feature:editor`      | Structured editor, verses, font size, paste, AI panel, history | 14–18, 21, 24, 34 |
| `:feature:ideas`       | Ideas box                                                      | 38, 39            |
| `:feature:settings`    | Profile, sync, AI, texts, licences                             | 29                |
| `:core:design`         | Theme, palettes light and dark, glass surfaces, motion tokens  | 02, 04, 44        |
| `:core:data`           | DataStore preferences, Room database                           | 05                |
| `:core:phonetics`      | Phonetic engines, syllables, rhymes, paste splitter (pure Kotlin) | 17, 19–23      |
| `:core:sync`           | Sync repository, Google Drive, conflicts, restore               | 26–28, 30         |
| `:core:ai`             | AI providers (on-device, online)                               | 31–33, 35–37      |

### Design tokens

| Token     | Light     | Dark      | Usage                                  |
|-----------|-----------|-----------|----------------------------------------|
| magenta   | `#E0287D` | `#E0287D` | Brand, main actions, selection         |
| orange    | `#FF7A1A` | `#FF7A1A` | AI actions only                        |
| turquoise | `#00B3A6` | `#3FD9CC` | Sync, states                           |
| yellow    | `#FFC93C` | `#FFC93C` | Small touches (nib of the logo, ideas) |
| ground    | `#F6F3F9` | `#0F1018` | Background                             |
| ink       | `#1E1B2E` | `#F4F1F8` | Main text                              |
| muted     | `#5E5A6B` | `#A9A6B8` | Secondary text                         |

All the tokens are in `GriotPalette` (`:core:design`); `GriotPaletteTest` checks 4.5:1 for every text colour. Filled
buttons with white text use `accentFill` `#DB2277` (white on `#E0287D` is 4.4:1). Turquoise and orange text in light
mode are `#006D65` and `#A04800`, darker than the mockups so they pass on their soft fills.

Rhyme colours, in order of appearance: turquoise, orange, magenta, yellow, blue, green, violet, ochre; only the
rhyming ending is highlighted and the margin letter (A, B, C…) always doubles the colour.

iOS-inspired, modern look: translucent "glass" surfaces with a background blur (Haze) on capable phones, and an
opaque tinted fallback on the others (GRI-04). Outfit for titles and lyrics, Inter for the interface (to embed,
GRI-02). Cards 18 to 24 radius, touch targets ≥ 44 dp.

## Progress

- [x] **GRI-01** — multi-module project, convention plugins, Hilt, type-safe navigation with placeholder screens
  (onboarding → library → editor, ideas, settings), DataStore (`UserPreferencesRepository`: onboarding done or not),
  Room plugin wired in `:core:data` (the database class comes with its first entities in GRI-05), light and dark
  palettes, temporary launcher icon, French default and English locales. Builds and runs on the phone; debug
  (`com.griot.debug`) and release (`com.griot`) install side by side. ktlint checks the Android sources through
  `kotlinScriptAdditionalPaths` (the plugin does not see AGP 9's built-in Kotlin). CI workflow ready; the repository
  has no GitHub remote yet.
- [x] **GRI-02** — Griot theme: palette (light and dark), eight rhyme colours (`RhymeColor`,
  `GriotTheme.rhymeColor(index)`), Outfit and Inter embedded (OFL, `core/design/licenses/`), type scale and
  `LyricTextStyle`, radii and spacing, contrast test, preview catalogue (`catalog/ThemeCatalog.kt`).
- [x] **GRI-03** — logo masters in `core/design/logo/` (SVG), `logo_quill` (light and night), silhouette and
  notification icon; `LogoTile` and the `backdrop` modifier (blurred shapes as radial gradients) in `:core:design`;
  system splash (`splash_icon`, one vector per mode) handed off to `LaunchScreen` (2 s) at the same place; frosted
  adaptive launcher icon with a monochrome layer.
- [x] **GRI-04** — glass layer from Cauri in `core/design/glass/`: `supportsGlassBlur()` (chosen once in
  `GriotApplication`), `GlassScreen` (background, content, overlay layers), `GlassSurface` (Haze blur or opaque
  tint); `GriotBottomBar` with « Chansons », « Idées », « Réglages » on the tabs only; `navigateToTab` keeps
  « Chansons » at the bottom of the back stack. Screens are transparent over the shared `screenBackdropShapes()`.
- [x] **GRI-44** — `GriotMotion` (from Cauri) through `GriotTheme.motion`: durations `SHORT`/`MEDIUM`/`LONG`, easings,
  screen, tab, bar, sheet and item transitions, `colorChange()` for rhyme colours; everything snaps with « Supprimer
  les animations ». `AnimatedPresence` and `RollingNumber` (syllable counts) in `:core:design`. Screens slide, tabs
  fade, the bar rises and goes down, the launch screen fades into the first screen.
- [x] **GRI-05** — `GriotDatabase` (version 1, schema in `core/data/schemas/`): songs, sections, verses, versions,
  ideas and `sync_state`. Every content row has a UUID, `updatedAt` and a `deleted` flag (deletes are `markDeleted`).
  A repeat section points to its source (`repeatOfSectionId`) and holds no verses; `VerseDao.observeBySection`
  resolves it. A row has changes to push when its `updatedAt` is after its `sync_state.syncedUpdatedAt`. Entities
  and DAOs are internal; enums (`SongLanguage`, `SectionKind`…) are public in `core.data.model`.
- [x] **GRI-06** — `AppLanguageRepository` (`:core:data`) on the per-app language API: AppCompat below Android 13
  (`MainActivity` is an `AppCompatActivity`, AppCompat theme, `AppLocalesMetadataHolderService`), the system above;
  the language applies at once and survives a restart. Temporary language choice on the settings placeholder until
  GRI-29. detekt rule `HardcodedUiText` (`:detekt-rules`) rejects literal text passed to UI parameters outside
  previews. MockK is in the catalog for static platform APIs.
- [x] **GRI-07** — `WelcomeScreen` from the mockup (logo tile, promise, « Commencer »). « Restaurer depuis Google
  Drive » shows only when `onboardingGraph` gets an `onRestore`: `:app` passes `null` until GRI-30.
- [x] **GRI-08** — `NameScreen` (step 1/4) on the shared `OnboardingStep` layout (back, progress, texts, content and
  actions slots). `GriotButton` and `GriotTextField` in `:core:design`. `UserPreferencesRepository` is now an
  interface with `UserPreferencesRepositoryImpl` and holds `authorName`; fakes in `:core:data` test fixtures. The
  name step goes on to the library until GRI-09 adds the next step.
- [x] **GRI-09** — `LanguageScreen` (step 2/4): French or English, preselected from the phone; « Continuer » saves
  `defaultSongLanguage` (preferences), moves on, then applies the interface language (in that order, as Android
  redraws the screens). The language step goes on to the library until GRI-10 and GRI-11.
- [ ] **Next tickets:** GRI-12, GRI-13, GRI-14 (GRI-10 waits for GRI-27, GRI-11 for GRI-17).

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on a `gri-0xx-description` branch, with small, readable commits.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the end.
- If a product choice is not settled by the documents, ask rather than decide alone.
