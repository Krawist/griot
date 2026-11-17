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
- [x] **GRI-12** — `LibraryScreen`: greeting, search (accents, case and apostrophes ignored, on titles and verses),
  filters (automatic-language songs only under « Toutes » and « Brouillons »), song cards last changed first,
  « Nouvelle chanson » (`SongRepository.createSong`: draft, one verse section, one empty verse) opening
  `EditorRoute(songId)`. `SongDao.observeLibrary` reads each card in one query. `RelativeTime` in `:core:design`.
  The rhyme dots and scheme of each card's first section (`observeFirstSections`, `rhymeLetters` in
  `:core:phonetics`, shared with the editor) came after. Still to add: the sync pill (GRI-27). The editor must touch the song's `updatedAt`
  on every change, as the library sorts on it.
- [x] **GRI-13** — long press on a song: `SongActionsSheet` (rename, duplicate as « … (copie) », delete). Delete
  moves to the trash (`deleted` flag) with an `UndoBar` (`:core:design`, 5 s) in place of « Nouvelle chanson ».
  `TrashScreen` (library feature, `navigateToTrash()`, opened from the settings) restores; `MainViewModel` purges
  the songs in the trash for more than `SongRepository.TRASH_DAYS` (30) at each launch. A duplicate copies sections
  and verses, repeats pointing to the copied sections. `GriotTextField` puts the cursor at the end of a value set
  from outside; the library's root is focusable so a closing sheet does not focus the search field.
- [x] **GRI-14** — `EditorScreen` on `EditorRoute(songId)` (assisted `EditorViewModel`): editable title, language
  chip (read-only until GRI-18), sections with their labels (`sectionLabels`: verses always numbered, other kinds
  when there are several, a repeat takes its source's label), « ⋯ » sheet (rename, repeat further on, move up or
  down, delete with its repeats after a confirmation), « Ajouter une section ». `SongEditorRepository` (one
  transaction per change, each dating the song) gives a `SongDocument` whose repeats show their source's verses.
  Verses are typed in `SyncedTextField` (local while focused, one line). `GriotSheet`, `SheetAction` and
  `TextInputForm` are shared in `:core:design`. Rhyme scheme, syllables and sync state come with their tickets.
- [x] **GRI-15** — Enter splits a verse at the cursor (a pasted text: one verse per line, until GRI-17), Backspace
  at the start joins the previous verse of the section, Enter on an empty last verse closes the section and starts
  a « Couplet » below; the ViewModel's `VerseFocus` moves the focus and the cursor. Verses wrap and stay one.
  `SongEditorRepository.splitVerse`, `mergeVerses`, `closeSection`. Backspace is a key event: to confirm with the
  Samsung keyboard in use.
- [x] **GRI-16** — lyrics size 14–28 sp (18 by default) in `UserPreferencesRepository.editorFontSize`, for every
  song: « Aa » sheet with a slider in the editor's top bar, and a two-finger pinch on the lyrics
  (`pinchToResize`, one finger still scrolls). The pinch is not checked on the phone yet (adb has no multitouch).
- [x] **GRI-17** — `splitPastedText` (`:core:phonetics`, pure Kotlin): blank lines start sections, label lines
  (FR/EN, brackets, numbers, « x2 », colon) name them, identical blocks repeat the first one (a plain block sung again
  becomes a chorus), a lone label repeats the last section of its kind. In the editor a multi-line paste of several
  blocks fills the current section and adds the others after it (`pasteSections`), with an undo bar that puts the
  text back into one section (`undoPaste`). A real paste is not checked on the phone yet (no adb clipboard).
- [x] **GRI-18** — the language chip opens `LanguageSheet` (Français, English, Automatique) and reads « Auto · FR »
  once detected. `SongLanguageDetector` (`:core:data`, ML Kit language-id with its model on board) runs on the whole
  song 1 s after the last change; `EditorUiState.writingLanguage` is the chosen or detected language, for the
  phonetic engines. The editor's sheets are one `EditorSheet` state.
- [x] **GRI-19 to GRI-22** — `:core:phonetics`: `PhoneticEngine` (transcription, syllables, rhyme key and range),
  `PhoneticEngines` (one per app, from `:app`'s `PhoneticsModule`), `FrenchEngine` on Lexique 3.83 (CC BY-SA 4.0)
  and `EnglishEngine` on the CMU dictionary (BSD), as gzipped `word<TAB>phonemes` tables built by
  `tools/phonetics/build_tables.py`; grapheme guesses for unknown words. Spoken syllables (synérèse), mute e
  rules of French songs. Reference sets of 50 French and 40 English verses (≥ 95 % exact). In the editor: rhyme
  letters by order of appearance per section, the scheme in the header, the rhyming ending highlighted (only when
  another verse of the section rhymes), counts rolling and lighter when guessed, glass toolbar with « Section » and
  « Rimes » (`showRhymes` preference). Licences still to show in the settings (GRI-29).
- [x] **GRI-23** — each engine reads the other language's words (`PhoneticEngines` wires them; an English word
  ending a French verse rhymes by the English engine). `SyllableCount.guessedWords` lists the words in no dictionary;
  a long press on a lighter count opens `CountCorrectionSheet`: the first unknown word gets the syllables that make
  the count given, kept in `word_corrections` (Room, database version 2, auto-migration) and applied by
  `PhoneticEngine.setCorrections` in every song.
- [x] **GRI-11** — `ImportScreen` (onboarding step 4/4; the sync step GRI-10 is not built yet): Samsung Notes (help
  sheet, then the picker), files (.txt, .md, several at once, through the system picker) or a pasted text.
  `TextImporter` (`:core:data`) makes one song per text (in progress, automatic language, named after the file,
  the note's title line left out) split by `splitPastedText`; empty or unreadable files are reported by name.
  `DocumentReader` reads the picked files. `toNewSections` moved to `:core:data`. The Samsung Notes steps are not
  checked on a real Samsung Notes yet.
- [x] **GRI-24** — `VersionRepository` (`:core:data`): a verse version before the first change of a burst and 2 s
  after the last one, a song version (JSON `SongSnapshot`) at most every 5 min; restores keep the text before and
  add a « Restaurée » version; `thin()` at launch keeps one version per day after 7 days. `HistoryScreen`
  (`HistoryRoute`, « Versions » in the editor toolbar): « Ce vers » / « Toute la chanson », changed words or lines
  highlighted (`changedWords`), « Restaurer » and « Copier ». The editor's saves run in order (a `Mutex`).
- [x] **GRI-25** — `MarkdownExporter` (`:core:data`): a song as `# Title`, `*Author*`, `## Label` and its verses
  (repeats written out), or every song in `Griot-<date>.zip`, in the cache's `exports/`; `FileShareSheet`
  (`:core:design`, `FileProvider` `${applicationId}.exports`) shares or saves to a folder. From the editor's new
  « ⋯ » menu and from the settings placeholder. Section labels (`SectionLabel`, `sectionLabels`, `words`) and their
  words now live in `:core:data`.
- [x] **GRI-38** — `IdeasScreen` (« Idées » tab): a text idea saved on « Envoyer », an image idea from the photo picker
  (copied to `files/ideas/`), cards newest first with « Libre » or the song's title, « Rattacher » (`SongPickerSheet`)
  or « Ouvrir »; long press: edit, link, free, new song from the idea (first verse), delete with an undo bar.
  `IdeaRepository` (`:core:data`).
- [x] **GRI-39** — the song's « ⋯ » menu lists its linked ideas; a tap adds one as a verse at the end of the song
  (`SongEditorRepository.appendVerse`).
- [x] **GRI-29** — `SettingsScreen` from the mockup: grouped glass cards (`SettingsGroup`, `SettingsRow` with a tinted
  icon box, `SettingsValue`, `SettingsSwitch`, `SettingsButton`). Profil: name (`NameSheet`) and app language
  (`AppLanguageSheet`; Français or English also sets the language of new songs). Synchronisation and Aide IA are shown
  dimmed, « Pas encore disponible », until GRI-27 and GRI-32/33. Textes: import (`navigateToImport()`, the onboarding's
  import without its progress, closing on « Terminé »), export everything, trash. Apparence: theme (the phone's),
  lyrics size (set with « Aa »), licences (`LicencesSheet`). `PlaceholderScreen` is gone.
- [x] **GRI-26** — `:core:sync`: `SyncRepository` (`status` flow: off, syncing, synced, pending, offline, error, with
  the pending count and the last sync; `sync()` pulls, applies, then pushes) on a `RemoteStore` interface
  (`push`, `pull(cursor)`), bound optionally (`@BindsOptionalOf`): without one, sync is off. `NetworkMonitor` on
  the default network. In `:core:data`, `LocalSyncSource` lists the rows to push (`SyncDao`, rows without
  `sync_state` or changed since) as `SyncRecord`s (the entity as JSON), marks them pushed, and applies remote rows
  (missing → added, the most recent `updatedAt` wins, parents first). The last sync time and cursor are preferences.
- [x] **GRI-28** — `LocalSyncSource.apply` settles a row changed on both devices since its last sync: the most recent
  wins; a verse's losing text becomes a version « Autre appareil » (`SYNC_CONFLICT`) and a notice in
  `sync_conflicts` (database version 3, local only); an idea's losing text stays as an idea of its own. The editor
  shows `ConflictNotice` under its top bar: « Voir » opens the history of the first verse and forgets its notice,
  « Ignorer » forgets the song's notices (`VersionRepository.observeConflicts`, `dismissVerseConflicts`,
  `dismissSongConflicts`). Checked on the phone with a hand-made conflict; no real two-device sync until GRI-27.
- [x] **GRI-31** — `:core:ai`: `AiAssistant` (`rephrase`, `critique`, `location`) picks the phone's model first, then
  the online one, among the `AiProvider`s bound into a set (empty until GRI-32/33: no model, `null` answers). A
  provider only runs its model (`generate(AiPrompt)`); prompts (`rephrasePrompt`, `critiquePrompt`: language,
  section, neighbouring verses, target count, the section's counts for the rhythm), the parsing (`answerLines`) and
  the checks are shared. `checkSuggestions` recounts every suggestion with the phonetic engine and drops those that
  do not rhyme like the verse or are more than one syllable away.
- [x] **GRI-35** — `PhoneticEngine.rhymingWords(verse)`: the dictionary words with the verse's rhyming ending (the
  engine of its last word's language), at most 12 per syllable count from 1 to 4 (`chooseRhymes`), the most frequent
  first: `fr-frequent.txt.gz` (Lexique films + books) and `en-frequent.txt.gz` (FrequencyWords, OpenSubtitles 2018,
  CC BY-SA 4.0; only these English words are offered, the CMU dictionary is full of names), built by
  `build_tables.py`. Listed in the settings' licences.
- [x] **GRI-34** — « Aide IA » (orange) in the editor toolbar opens `AiPanelSheet` on the verse being edited (else the
  first verse with text): place and count, the verse, tabs « Rimes », « Reformuler », « Critiquer », and where the
  answers come from. `AiPanelViewModel` (assisted: song, verse, language) reads the verse's section and neighbours,
  its rhymes (words of the song crossed out), and asks `AiAssistant` when a model tab opens or on « Autres idées »;
  without a model the tabs say so. A rhyme is inserted at the verse's cursor (`VerseInput.onCursor`, `insertWord`),
  a suggestion replaces the verse and is recorded as « Suggestion IA » (`onUseSuggestion`). Rhymes and insertion
  checked on the phone; the model tabs wait for GRI-32/33.
- [ ] **Next tickets:** GRI-27 (Drive: needs a Google Cloud OAuth client), then GRI-10 and GRI-30; GRI-32/33 (models:
  need setup), then GRI-36/37 to check with a real model.

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on a `gri-0xx-description` branch, with small, readable commits.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the end.
- If a product choice is not settled by the documents, ask rather than decide alone.
