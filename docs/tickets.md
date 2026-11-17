# Griot — MVP tickets

> Reference documents kept in Claude (November 16, 2026): the vision and features document, https://claude.ai/artifact/TFJQahYirQ72jEVJMs1Xoy, and the design canvas, https://claude.ai/artifact/GtB94FRDSFC6mVmKKVfCfq. If this file and the vision document differ on scope, the vision document prevails. Update this copy when they change.

This document splits the Griot MVP into 45 tickets over three phases. Phase 1 gives an app usable every day that replaces Samsung Notes for songwriting: a structured editor, live syllables and rhymes in French and English, version history and Google Drive sync. Phase 2 adds the AI panel and the ideas box. Phase 3 adds the features listed under "Ensuite" in the vision document.

Indicative sizes: S for a day at most, M for two or three days, L for about a week.

User-facing texts are quoted in French, the app's default language. Mockups are artboards of the design canvas, named here by their title; every screen has a light version and, below it on the canvas, a dark one. Song titles, verses and names in the mockups are examples.

## Order of work

The skeleton, the theme and the database come first, since every feature depends on them. The editor and the phonetic engines come next: they are what makes Griot better than a notes app, and they need real lyrics to be tuned, so the import arrives early too. Sync and version history must ship before the app holds the only copy of any text: the whole project exists because texts were lost once. The AI (phase 2) comes after, because it builds on the editor's selection and on the phonetic engine, which checks every suggestion.

## Phase 1 · Technical foundations

### GRI-01 · Set up the multi-module project and CI

Set up the multi-module Gradle project (Kotlin DSL, version catalog, convention plugins) with Compose, Hilt, Room, DataStore, WorkManager and type-safe navigation, on the same structure as Cauri (`:app`, `:core:*`, `:feature:*`).

- The app compiles, launches and goes from a placeholder onboarding to a placeholder library.
- CI runs ktlint, detekt, unit tests and the debug build on every pull request.
- Debug and release variants are signed separately; both can be installed side by side.

Size: M.

### GRI-02 · Build the Griot Compose theme

Turn the mockups into a Compose theme: the Wax palette (magenta `#E0287D`, orange `#FF7A1A`, turquoise `#00B3A6`, sun yellow `#FFC93C`) on a light ground (`#F6F3F9`) and a night ground (`#0F1018`), Outfit for titles and lyrics, Inter for the interface (both embedded, OFL), radii and spacing.

- The theme follows the phone's dark mode; screens read colours only from `GriotTheme.palette`.
- Magenta is the brand and main action colour, orange is reserved for AI actions, turquoise for sync and states.
- The rhyme palette is a separate set of eight colours (turquoise, orange, magenta, yellow, blue, green, violet, ochre), each with a light and a dark value for the underline, the highlight and the margin letter.
- Text contrast reaches at least 4.5:1 in both modes (checked by a unit test).
- A catalogue of previews shows every token and component.

Size: M. Dependencies: GRI-01. Mockups: all screens.

### GRI-03 · Add the app logo and launch screen

The logo is the quill and wave: the frosted glass version in light mode, the night glass version in dark mode. The launch screen shows it on the light or night ground, with the coloured blurred shapes behind and the "Griot" wordmark under it. To be done right after GRI-02.

- The system splash (`androidx.core:core-splashscreen`) shows the logo on the right ground for the current mode, so the hand-off to the Compose launch screen is seamless.
- The Compose launch screen reproduces the mockup; the blur is drawn in the screen, as the system splash cannot blur.
- The launcher icon is the frosted glass version (an adaptive icon cannot follow dark mode), with a monochrome layer for Android 13 themed icons.
- The notification icon is the white silhouette of the quill.
- The logo files (SVG master, vector drawables) live in `:core:design`.

Size: M. Dependencies: GRI-02. Mockups: « Icône et logotype », « Lancement · clair », « Lancement · sombre ».

### GRI-04 · Build the glass surfaces and the bottom bar

A reusable surface with background blur and transparency (Haze), used for the bottom bar, the editor toolbar and sheets, with an opaque fallback on devices without blur. The floating bottom bar holds « Chansons », « Idées » and « Réglages ».

- Blur is on only where `RenderEffect` is available and the device is not flagged as low-end; the fallback is chosen once at startup.
- The selected tab is highlighted in magenta; back from a tab returns to « Chansons », then leaves the app.
- Features never navigate to each other directly: `:app` wires them through callbacks.

Size: M. Dependencies: GRI-02. Mockups: « Bibliothèque », « Boîte à idées », « Réglages ».

### GRI-44 · Add animations and transitions across the app

Every change of screen and state is animated in one consistent motion language, from the first screens built, so features never ship without motion. To be done as early as possible, right after GRI-04: screens slide or fade, sheets rise from the bottom, rhyme colours appear softly as the user types, and counts roll to their new value.

- Durations and easings come from shared motion tokens in `:core:design`.
- Android's « Supprimer les animations » setting is respected.

Size: M. Dependencies: GRI-04.

### GRI-05 · Set up the Room database

Room tables: songs, sections, verses, versions, ideas and sync state. A song has a title, a writing language (`fr`, `en` or automatic) and a status (draft, in progress). A section has a kind (intro, verse, chorus, bridge, outro, other), a name and an order; a chorus can be a repeat of another section. A verse is one line of text with an order. Schemas are exported for migrations.

- Every row carries a stable UUID, a last modified time and a deleted flag, so sync can merge and deletes travel between devices.
- A repeated chorus points to its source section and is never copied.
- DAOs are tested on an in-memory database with the bundled SQLite driver.

Size: M. Dependencies: GRI-01.

### GRI-06 · Set up French and English localisation

All interface texts live in string resources, in French (default) and English. The interface language follows the choice made in the onboarding (GRI-09), then the settings.

- No hard-coded user-facing string in the code (checked by a lint rule).
- Switching the language in the settings applies without restarting the app (per-app language API).

Size: S. Dependencies: GRI-01.

## Phase 1 · Onboarding

### GRI-07 · Build the welcome screen

Logo, wordmark, « Écris tes chansons. Garde ta voix. », the « Commencer » button and the « Restaurer depuis Google Drive » link.

- « Restaurer depuis Google Drive » runs the restore flow (GRI-30) and skips the rest of the onboarding when it succeeds.

Size: S. Dependencies: GRI-03. Mockup: « Onboarding · bienvenue ».

### GRI-08 · Build the name screen

First name or artist name, used to sign exported texts. Saved locally.

- « Continuer » stays enabled; an empty name is pointed out after a first attempt.
- The progress bar shows step 1 of 4.

Size: S. Dependencies: GRI-05. Mockup: « Onboarding · nom ».

### GRI-09 · Build the language screen

Choice between « Français » and « English ». It sets the interface language and the default writing language of new songs.

- French is preselected when the phone is in French, English otherwise.
- The note about mixed texts is visible: Griot detects the dominant language of each song.

Size: S. Dependencies: GRI-06. Mockup: « Onboarding · langue ».

### GRI-10 · Build the sync screen

Invites the user to connect Google Drive, with automatic sync on by default. « Plus tard » skips the step; sync can be enabled later from the settings.

- « Connecter Google Drive » signs in with the Drive `appDataFolder` scope only, never full Drive access.
- If the user skips, the library shows a discreet reminder until sync is enabled or the reminder is dismissed.

Size: M. Dependencies: GRI-27. Mockup: « Onboarding · synchronisation ».

### GRI-11 · Build the import screen and the import flows

Three ways to bring existing texts: from Samsung Notes (exported as `.txt`), from files (`.txt` or `.md`), or by pasting a text. Each file becomes a song named after the file; its text goes through the paste splitter (GRI-17).

- Several files can be picked at once; a summary shows how many songs and sections were created.
- An empty or unreadable file is reported by name and does not stop the others.
- A step-by-step help explains how to export notes from Samsung Notes, checked on a real Samsung phone.
- « Commencer à écrire » opens the library.

Size: M. Dependencies: GRI-17. Mockup: « Onboarding · import ».

## Phase 1 · Library

### GRI-12 · Build the library screen

List of songs with title, language, first verse, rhyme scheme as coloured dots, number of sections and last change. Search on titles and verses, filters « Toutes », « Français », « English », « Brouillons ». The sync pill shows the time of the last sync and runs a sync when tapped.

- Songs are sorted by last change, newest first.
- Search matches accents loosely (« neons » finds « néons »).
- « Nouvelle chanson » creates an empty song and opens the editor.

Size: M. Dependencies: GRI-04, GRI-05. Mockup: « Bibliothèque ».

### GRI-13 · Rename, duplicate and delete songs

Long press on a song opens rename, duplicate and delete. A deleted song goes to a trash kept 30 days, then is removed everywhere.

- Delete shows an undo bar for a few seconds.
- The trash is reachable from the settings and can restore a song.

Size: S. Dependencies: GRI-12.

## Phase 1 · Editor

### GRI-14 · Build the structured editor

A song is a list of sections, each with its label (« Couplet 1 », « Refrain », « Pont »…) and its rhyme scheme. Sections can be added, renamed, reordered and deleted; a chorus can be repeated without being copied.

- Section labels are numbered automatically per kind (« Couplet 1 », « Couplet 2 ») unless renamed.
- Editing a repeated chorus edits every repeat.
- The top bar shows the title, the sync state and the song's language.

Size: L. Dependencies: GRI-05. Mockup: « Éditeur ».

### GRI-15 · Write verses with line breaks and auto-wrap

A verse is one line of the model. Pressing Enter creates a new verse below the current one. A verse longer than the screen wraps onto several visual lines and stays a single verse: one syllable count, one rhyme letter, shown on its first visual line.

- Enter in the middle of a verse splits it into two verses at the cursor.
- Backspace at the start of a verse merges it with the previous one.
- Enter on an empty verse at the end of a section closes the section and starts a new one below (to be confirmed in use).
- Wrapping follows the screen width and the font size (GRI-16), never inserts characters, and never changes the stored text.

Size: M. Dependencies: GRI-14. Mockup: « Éditeur ».

### GRI-16 · Change the editor font size

The user can enlarge or reduce the lyrics text: with a pinch in the editor, or with a size slider in the « Aa » menu of the editor's top bar.

- Range from 14 to 28 sp, 18 sp by default, in steps of 1 sp; the size is saved in DataStore and applies to every song.
- The system font scale still applies on top of the chosen size.
- Syllable counts, rhyme letters and highlights scale with the text and stay aligned.

Size: S. Dependencies: GRI-15. Mockup: « Éditeur ».

### GRI-17 · Split a pasted long text into sections

Pasting a text of several lines creates one verse per line and, when possible, splits it into sections. The same splitter is used by the import (GRI-11).

- A blank line between two blocks starts a new section.
- A line that is a section label becomes the name of the section that follows and is not kept as a verse: « Couplet », « Refrain », « Pont », « Intro », « Outro », « Verse », « Chorus », « Bridge », « Pre-chorus », with or without a number, brackets or parentheses (« [Chorus] », « (Refrain) », « Couplet 2 : »).
- A block identical to an earlier block is recognised as a repeat of that section (usually the chorus).
- A text with no blank line and no label stays one section.
- After a paste that created several sections, a bar « 4 sections créées · Annuler » restores the text as a single section.
- The splitter is pure Kotlin, covered by JVM tests on real Samsung Notes exports and lyrics with and without labels.

Size: M. Dependencies: GRI-14.

### GRI-18 · Set and detect the song's language

Each song has a writing language: French, English or automatic. In automatic mode, the dominant language of the text decides which phonetic engine counts syllables and finds rhymes, and which language the AI answers in.

- The language chip in the editor's top bar shows the current language and lets the user force it.
- Detection runs on the whole song, not per verse, so a few English words in a French song do not switch the engine.
- Detection is on-device (ML Kit language identification) and never sends text anywhere.

Size: S. Dependencies: GRI-14. Mockup: « Éditeur » (chip « FR »).

### GRI-45 · Give the songs and the lyrics the whole screen while scrolling

Once many songs are imported, the song list scrolls with stutters, and its header, the « Nouvelle chanson » button and the bottom bar leave little room for the songs; the editor's top bar and toolbar do the same to the lyrics. In the song list and in the editor, scrolling down hides these controls so the content takes the whole screen; scrolling up brings them back. Once scrolled down, a small floating button jumps back to the top.

- Scrolling the song list (40 songs and more) and a long song (40 verses and more) stays smooth on the phone.
- The controls slide away and back with the shared motion tokens; they come back at the top of the list or of the song, and when a verse takes the focus.
- « Haut de la liste » and « Haut de la chanson » appear only away from the top, and take the screen back to its top.
- Scrolling and the jump to the top behave the same on both screens: one shared implementation.

Size: S. Dependencies: GRI-04, GRI-12, GRI-14, GRI-44. Mockups: « Bibliothèque », « Éditeur ».

## Phase 1 · Phonetics

### GRI-19 · Define the phonetic engine interface

A `PhoneticEngine` interface per language: phonetic transcription of a word, syllable count of a verse, and rhyme key of a verse ending. The editor and the AI only depend on this interface.

- Engines are pure Kotlin in `:core:phonetics`, with no Android dependency, and covered by JVM tests.
- Results are cached per word; counting a verse never blocks the main thread.

Size: S. Dependencies: GRI-01.

### GRI-20 · Build the French engine on Lexique

French transcriptions come from the Lexique database, embedded in the app as a compact table. Syllable counting applies song conventions: a final mute e is not counted, a mute e before a vowel is elided, a mute e between consonants counts, liaisons and elisions follow the written text.

- The licence of Lexique is checked and its attribution is shown in the settings.
- Counts match a reference set of at least 200 verses counted by hand, with at least 95 % exact.
- Unknown words (names, slang, Cameroonian words) fall back on grapheme rules (GRI-23).

Size: L. Dependencies: GRI-19.

### GRI-21 · Detect and colour rhymes

Verses whose endings share the same rhyme key get the same colour and the same letter in the margin. Letters and colours are given in order of appearance in each section (A turquoise, B orange, C magenta, and so on); beyond eight different rhymes, colours are reused.

- Only the rhyming ending is highlighted (light background and coloured underline), never the whole word.
- The margin letter always doubles the colour, for colour-blind users.
- The section header shows its scheme (« ABAB »).
- The « Rimes » button of the toolbar hides and shows the colours.

Size: M. Dependencies: GRI-20. Mockup: « Éditeur ».

### GRI-22 · Build the English engine on the CMU dictionary

English transcriptions come from the CMU Pronouncing Dictionary, embedded in the app. Syllables are counted from vowel phonemes; rhyme keys go from the last stressed vowel to the end of the word.

- The licence of the dictionary is checked and its attribution is shown in the settings.
- Counts match a reference set of at least 200 English verses with at least 95 % exact.

Size: M. Dependencies: GRI-19.

### GRI-23 · Handle unknown words and mixed texts

Words missing from the dictionaries get a transcription from grapheme rules of the song's language. The syllable count of a verse containing an unknown word is shown in a lighter colour.

- A long press on a verse's count lets the user correct it; the correction is kept for that word.
- Words from the other language are transcribed with its engine when they are found there.

Size: M. Dependencies: GRI-20, GRI-22.

## Phase 1 · Saving and sync

### GRI-24 · Keep the version history

Every change is saved immediately. A version is recorded per verse when the user pauses typing, and per song at most every few minutes. The history screen shows the versions of a verse or of the whole song, with the changed words highlighted, and can restore or copy any of them.

- Restoring a version creates a new version; nothing is ever overwritten.
- Versions created from an AI suggestion are marked « Suggestion IA ».
- Old versions are thinned out over time (all versions of the last week, then one per day), never below one per day.

Size: L. Dependencies: GRI-15. Mockup: « Historique ».

### GRI-25 · Export everything as Markdown

Export of one song or of all songs as Markdown files, with section labels as headings and the author's name. Shared through the Android share sheet or saved to a folder.

- The export opens and reads correctly in any text editor.
- Exporting all songs produces one file per song in a zip.

Size: S. Dependencies: GRI-14.

### GRI-26 · Define the sync repository

A `SyncRepository` interface that pushes local changes and pulls remote ones, independent of where data is stored. The first implementation targets Google Drive (GRI-27); a Micronaut backend can replace it later without touching the features.

- Local writes never wait for the network.
- The sync state (synced, pending, offline, error) is exposed as a flow for the library, the editor and the settings.

Size: S. Dependencies: GRI-05.

### GRI-27 · Sync with Google Drive automatically and on demand

The Drive implementation stores the songs in the app's `appDataFolder`. A WorkManager job pushes changes a few seconds after the last edit, with a network constraint; a « Synchroniser maintenant » button runs a sync at once.

- Without network, changes are kept and sent as soon as the network comes back.
- The time of the last sync is visible in the library, the editor and the settings.
- Sync can be turned off; local data is never deleted when it is.

Size: L. Dependencies: GRI-26. Mockups: « Bibliothèque », « Éditeur », « Réglages ».

### GRI-28 · Resolve sync conflicts without losing text

When the same verse changed on two devices since the last sync, both versions are kept: the most recent becomes the current text, the other is saved in the history and the user is told.

- No conflict ever discards a version.
- A notice in the editor opens the history on the conflicting verse.

Size: M. Dependencies: GRI-24, GRI-27.

### GRI-29 · Build the settings screen

Profile (name, app language), sync (Drive account, automatic sync, « Synchroniser maintenant »), AI (on-device AI, online AI), texts (import, export, trash), appearance, and the licences of the dictionaries.

Size: M. Dependencies: GRI-04. Mockup: « Réglages ».

### GRI-30 · Restore from Google Drive

On a new install, « Restaurer depuis Google Drive » downloads every song, version and idea, then opens the library.

- The restore shows its progress and can be resumed after an interruption.
- A restore never duplicates songs already on the phone.

Size: M. Dependencies: GRI-27.

## Phase 2 · AI

### GRI-31 · Define the AI provider interface

An `AiProvider` interface with three operations: rephrase a verse, critique a verse, and (later) explore a theme. Two implementations: on-device (GRI-32) and online (GRI-33). The app picks on-device when available, online otherwise, as allowed by the settings.

- Every prompt includes the song's language, the section, the neighbouring verses and the target syllable count.
- Every suggestion returned by a model is checked by the phonetic engine: its syllable count and rhyme are computed by code, never trusted from the model.

Size: M. Dependencies: GRI-19.

### GRI-32 · Add the on-device provider (Gemini Nano)

Implementation on the ML Kit GenAI Prompt API, using the Gemini Nano model shared on the device. Used for critiques when the device supports it.

- Availability is checked at startup and when the model finishes downloading; unsupported devices fall back on the online provider.
- The text never leaves the phone with this provider, and the panel says so.

Size: M. Dependencies: GRI-31.

### GRI-33 · Add the online provider

Implementation on a small, low-cost cloud model, behind the same interface. For personal use, the API key is stored on the device in the Android Keystore; a proxy (a free Cloudflare Worker) will be required before any public release.

- Free tiers are used only during development, since some of them may train on the texts sent.
- Calls time out after a few seconds and show a retry, never a blocking spinner.

Size: M. Dependencies: GRI-31.

### GRI-34 · Build the AI panel

A sheet opened from the « Aide IA » button on the selected verse, with three tabs: « Rimes », « Reformuler », « Critiquer ». The verse, its place in the song and its syllable count are shown at the top; the bottom line says whether the answer comes from the phone or from the internet.

- Orange marks every AI element; the suggestions are always shown next to the text, never in its place.
- « Autres idées » asks for new suggestions.

Size: M. Dependencies: GRI-21, GRI-31. Mockup: « Panneau IA ».

### GRI-35 · Suggest rhymes

The « Rimes » tab lists words that rhyme with the end of the verse, from the phonetic dictionary, grouped by number of syllables. Words already used in the song are crossed out. Tapping a word inserts it at the cursor.

- Works offline, without any model.

Size: M. Dependencies: GRI-34. Mockup: « Panneau IA » (tab « Rimes »).

### GRI-36 · Rephrase a verse

The « Reformuler » tab proposes three versions of the verse that keep its meaning, its rhyme and a close syllable count. Each shows its count and what it changes; « Utiliser » replaces the verse and records a version marked « Suggestion IA ».

- A suggestion whose rhyme or count differs from what is announced is dropped before display.

Size: M. Dependencies: GRI-34. Mockup: « Panneau IA » (tab « Reformuler »).

### GRI-37 · Critique a verse

The « Critiquer » tab gives at most three short remarks on the verse: clichés, weak images, breaks of tone, rhythm compared with the other verses of the section.

- Rhythm remarks use the counts of the phonetic engine.
- Remarks never contain a rewritten verse.

Size: M. Dependencies: GRI-34. Mockup: « Panneau IA » (tab « Critiquer »).

## Phase 2 · Ideas box

### GRI-38 · Build the ideas box

A sentence, a title or an image is saved in two seconds from the « Idées » tab. Ideas are listed newest first, free or linked to a song.

- Saving works offline and syncs like songs.
- An idea can be edited, deleted or moved into a song as a new verse.

Size: M. Dependencies: GRI-04, GRI-05. Mockup: « Boîte à idées ».

### GRI-39 · Link an idea to a song

« Rattacher » links an idea to an existing song or starts a new song from it. Linked ideas appear in the song's menu.

Size: S. Dependencies: GRI-38. Mockup: « Boîte à idées ».

## Phase 3 · After the MVP

### GRI-40 · Learn the author's style

The online provider receives a short sample of the author's own verses as examples, so that rephrasings sound like them. The sample is chosen in the same language and, when possible, the same mood.

- The feature can be turned off in the settings; the sample never includes texts marked private.

Size: M. Dependencies: GRI-36.

### GRI-41 · Explore a theme

Before writing, the author asks for angles, images and vocabulary around a theme. The answer contains no ready-made verse.

Size: S. Dependencies: GRI-31.

### GRI-42 · Capture ideas by voice and from a widget

A voice note is transcribed on the device and saved as an idea, with the audio kept. A home screen widget and a quick settings tile open the capture directly.

Size: M. Dependencies: GRI-38. Mockup: « Boîte à idées » (micro button).

### GRI-43 · Attach a hummed melody to a section

The author records a melody and attaches it to a section; it plays from the editor. The audio syncs with the song.

Size: M. Dependencies: GRI-14, GRI-27.
