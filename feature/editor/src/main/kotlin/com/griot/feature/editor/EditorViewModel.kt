package com.griot.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.idea.IdeaRepository
import com.griot.core.data.language.SongLanguageDetector
import com.griot.core.data.model.ExportFile
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.VersionSource
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.VersionRepository
import com.griot.core.data.song.WordCorrectionRepository
import com.griot.core.data.song.sectionLabels
import com.griot.core.data.song.toNewSections
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.paste.PastedBlock
import com.griot.core.phonetics.paste.splitPastedText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** The editor of the song [songId]: every change is saved at once. */
// One function per change the editor makes, one dependency per kind of data it reads or writes.
@Suppress("TooManyFunctions", "LongParameterList")
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = EditorViewModel.Factory::class)
internal class EditorViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    private val editor: SongEditorRepository,
    private val songs: SongRepository,
    private val preferences: UserPreferencesRepository,
    private val detector: SongLanguageDetector,
    private val engines: PhoneticEngines,
    private val corrections: WordCorrectionRepository,
    private val versions: VersionRepository,
    private val exporter: MarkdownExporter,
    ideas: IdeaRepository,
) : ViewModel() {
    private val focus = MutableStateFlow<VerseFocus?>(null)
    private val pasteUndo = MutableStateFlow<PasteUndo?>(null)

    private val export = MutableStateFlow<ExportFile?>(null)

    // The verse being edited, whose history « Versions » opens.
    private val editedVerse = MutableStateFlow<String?>(null)

    // The verse and the place of its cursor, where a rhyme of the AI panel goes.
    private var cursor: Pair<String, Int>? = null

    // The versions waiting for the author to pause, by verse.
    private val pendingVersions = mutableMapOf<String, Job>()

    // The verse's saves, one after the other in the order typed.
    private val writes = Mutex()

    val uiState: StateFlow<EditorUiState> = editor.observeDocument(songId)
        .map { document ->
            if (document == null) {
                EditorUiState(loading = false, missing = true)
            } else {
                val labels = sectionLabels(document.sections)
                EditorUiState(
                    loading = false,
                    title = document.title,
                    language = document.language,
                    languageDetected = document.languageDetected,
                    languageChoice = when {
                        document.languageDetected -> LanguageChoice.AUTOMATIC
                        document.language == SongLanguage.ENGLISH -> LanguageChoice.ENGLISH
                        else -> LanguageChoice.FRENCH
                    },
                    sections = document.sections.mapIndexed { index, section ->
                        EditorSection(
                            section = section,
                            label = labels[index],
                            first = index == 0,
                            last = index == document.sections.lastIndex,
                            mergeable = section.repeatOf == null &&
                                document.sections.getOrNull(index - 1)?.let { it.repeatOf == null } == true,
                        )
                    },
                )
            }
        }
        .combine(focus) { state, focus -> state.copy(focus = focus) }
        .combine(preferences.editorFontSize) { state, size -> state.copy(fontSize = size) }
        .combine(pasteUndo) { state, undo -> state.copy(pasteUndo = undo) }
        .combine(editedVerse) { state, verse -> state.copy(editedVerse = verse) }
        .combine(export) { state, file -> state.copy(export = file) }
        .combine(ideas.observeSongIdeas(songId)) { state, linked -> state.copy(ideas = linked) }
        .combine(versions.observeConflicts(songId)) { state, verses -> state.copy(conflicts = verses) }
        .combine(preferences.showRhymes) { state, show -> state.copy(showRhymes = show) }
        .combine(corrections.observeCorrections(SongLanguage.FRENCH)) { state, words ->
            engines.french.setCorrections(words)
            state
        }
        .combine(corrections.observeCorrections(SongLanguage.ENGLISH)) { state, words ->
            engines.english.setCorrections(words)
            state
        }
        .map { state -> state.withSounds() }
        // The phonetics read their dictionaries and count every verse: never on the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), EditorUiState())

    init {
        // In automatic mode, the language is detected again 2 s after the last change, never while the author types;
        // the whole song is read, so a few English words do not switch a French song.
        viewModelScope.launch {
            editor.observeDocument(songId)
                .filterNotNull()
                .filter { it.languageDetected }
                .map { it.language to it.lyrics() }
                .distinctUntilChanged { old, new -> old.second == new.second }
                .debounce(DETECTION_DELAY_MILLIS)
                .collectLatest { (language, lyrics) ->
                    val detected = detector.detect(lyrics)
                    if (detected != null && detected != language) {
                        editor.setLanguage(songId = songId, language = detected, detected = true)
                    }
                }
        }
    }

    /** The history of the conflict on the verse [verseId] is opened: its notice goes. */
    fun onConflictShown(verseId: String) {
        viewModelScope.launch { versions.dismissVerseConflicts(verseId) }
    }

    /** Hides the notices of every conflict of the song; the versions stay in the history. */
    fun onConflictsDismissed() {
        viewModelScope.launch { versions.dismissSongConflicts(songId) }
    }

    /**
     * The author chose a language, or the automatic mode: the language is then detected at once, and kept when the
     * lyrics are too short to tell.
     */
    fun onLanguageSelected(choice: LanguageChoice) {
        viewModelScope.launch {
            when (choice) {
                LanguageChoice.FRENCH -> editor.setLanguage(songId, language = SongLanguage.FRENCH, detected = false)
                LanguageChoice.ENGLISH -> editor.setLanguage(songId, language = SongLanguage.ENGLISH, detected = false)
                LanguageChoice.AUTOMATIC -> {
                    val document = editor.observeDocument(songId).first() ?: return@launch
                    val language = detector.detect(document.lyrics()) ?: document.language
                    editor.setLanguage(songId = songId, language = language, detected = true)
                }
            }
        }
    }

    fun onShowRhymesChange(show: Boolean) {
        viewModelScope.launch { preferences.setShowRhymes(show) }
    }

    fun onTitleChange(title: String) {
        viewModelScope.launch { songs.renameSong(id = songId, title = title.singleLine()) }
    }

    fun onAddSection(kind: SectionKind) {
        viewModelScope.launch { editor.addSection(songId = songId, kind = kind) }
    }

    fun onRepeatSection(sectionId: String) {
        viewModelScope.launch { editor.repeatSection(sectionId) }
    }

    fun onRenameSection(sectionId: String, name: String) {
        viewModelScope.launch { editor.renameSection(sectionId = sectionId, name = name) }
    }

    fun onMoveSection(sectionId: String, offset: Int) {
        viewModelScope.launch { editor.moveSection(sectionId = sectionId, offset = offset) }
    }

    /** Merges the section [sectionId] into the one above it; the song as it was is kept in its history first. */
    fun onMergeSection(sectionId: String) {
        viewModelScope.launch {
            writes.withLock {
                versions.recordSong(songId)
                editor.mergeSection(sectionId)
            }
        }
    }

    fun onDeleteSection(sectionId: String) {
        viewModelScope.launch { editor.deleteSection(sectionId) }
    }

    /** Saves the verse as typed; a line break is not part of a verse (GRI-15 turns it into a new verse). */
    fun onVerseChange(verseId: String, text: String) {
        // The first change after a pause keeps the text from before it, so that no text is ever lost.
        val firstChange = verseId !in pendingVersions
        viewModelScope.launch {
            writes.withLock {
                if (firstChange) {
                    versions.recordVerse(verseId)
                    versions.recordSong(songId)
                }
                editor.updateVerse(verseId = verseId, text = text.singleLine())
            }
        }
        recordWhenPaused(verseId)
    }

    /** Adds the text of a linked idea as a new verse at the end of the song. */
    fun onAddIdea(text: String) {
        viewModelScope.launch { editor.appendVerse(songId = songId, text = text) }
    }

    /** Exports the song as Markdown, then shows what to do with the file. */
    fun onExport() {
        viewModelScope.launch { export.value = exporter.exportSong(songId) }
    }

    /** Saves the exported file in the document [uri] the user picked. */
    fun onSaveExport(uri: String) {
        val file = export.value ?: return
        viewModelScope.launch { exporter.save(file = file, uri = uri) }
    }

    fun onExportClosed() {
        export.value = null
    }

    /** The verse [verseId] took the focus, or no verse has it when `null`. */
    fun onVerseFocused(verseId: String?) {
        editedVerse.value = verseId
    }

    /** The cursor of the verse [verseId] is at [cursor]: a rhyme chosen in the AI panel goes there. */
    fun onVerseCursor(verseId: String, cursor: Int) {
        this.cursor = verseId to cursor
    }

    /** Inserts [word] at the cursor of the verse [verseId] (at its end when its cursor is unknown), then focuses it. */
    fun onInsertWord(verseId: String, word: String) {
        val text = verseText(verseId) ?: return
        val at = cursor?.takeIf { it.first == verseId }?.second ?: text.length
        val (inserted, after) = insertWord(text = text, cursor = at, word = word)
        onVerseChange(verseId = verseId, text = inserted)
        cursor = verseId to after
        focus.value = VerseFocus(verseId = verseId, text = inserted, cursor = after)
    }

    /**
     * Replaces the verse [verseId] by [text], a suggestion of the AI: its text before is kept, and the suggestion is
     * recorded as a version « Suggestion IA ».
     */
    fun onUseSuggestion(verseId: String, text: String) {
        viewModelScope.launch {
            writes.withLock {
                versions.recordVerse(verseId)
                editor.updateVerse(verseId = verseId, text = text.singleLine())
                versions.recordVerse(verseId = verseId, source = VersionSource.AI_SUGGESTION)
                versions.recordSong(songId)
            }
        }
        focus.value = VerseFocus(verseId = verseId, text = text.singleLine(), cursor = text.singleLine().length)
    }

    // A version of the verse, and of the song at most every few minutes, once the author paused typing it.
    private fun recordWhenPaused(verseId: String) {
        pendingVersions.remove(verseId)?.cancel()
        pendingVersions[verseId] = viewModelScope.launch {
            delay(VERSION_PAUSE_MILLIS)
            pendingVersions.remove(verseId)
            writes.withLock {
                versions.recordVerse(verseId)
                versions.recordSong(songId)
            }
        }
    }

    /**
     * The verse [verseId] was typed with line breaks, into [lines]; [cursor] is where the cursor was in the last one.
     * Enter on an empty last verse of a section closes the section and starts a new one; otherwise each line after
     * the first becomes a new verse, and the last one takes the focus.
     */
    fun onVerseLines(verseId: String, lines: List<String>, cursor: Int) {
        val section = sectionOf(verseId) ?: return
        val blocks = splitPastedText(lines.joinToString(separator = "\n"))
        if (blocks.size > 1) {
            paste(verseId = verseId, lines = lines, blocks = blocks)
            return
        }
        val closesSection = lines.size == 2 &&
            lines.all { it.isEmpty() } &&
            section.verses.size > 1 &&
            section.verses.last().id == verseId
        viewModelScope.launch {
            focus.value = if (closesSection) {
                VerseFocus(verseId = editor.closeSection(verseId), text = "", cursor = 0)
            } else {
                val added = editor.splitVerse(verseId = verseId, lines = lines)
                VerseFocus(
                    verseId = added.last(),
                    text = lines.last(),
                    cursor = cursor.coerceIn(0, lines.last().length),
                )
            }
        }
    }

    // A text that holds several sections: they are created, and the undo bar can turn them back into one section.
    private fun paste(verseId: String, lines: List<String>, blocks: List<PastedBlock>) {
        viewModelScope.launch {
            val paste = editor.pasteSections(verseId = verseId, sections = blocks.toNewSections())
            pasteUndo.value = PasteUndo(
                verseId = verseId,
                lines = lines.filter { it.isNotBlank() }.ifEmpty { listOf("") },
                paste = paste,
                sectionCount = blocks.size,
            )
        }
    }

    /** Turns the last paste back into one section, one verse per line. */
    fun onUndoPaste() {
        val undo = pasteUndo.value ?: return
        pasteUndo.value = null
        viewModelScope.launch { editor.undoPaste(paste = undo.paste, verseId = undo.verseId, lines = undo.lines) }
    }

    fun onPasteUndoDismissed() {
        pasteUndo.value = null
    }

    /** Backspace at the start of the verse [verseId], holding [text]: it joins the previous verse of its section. */
    fun onVerseBackspaceAtStart(verseId: String, text: String) {
        val verses = sectionOf(verseId)?.verses ?: return
        val previous = verses.getOrNull(verses.indexOfFirst { it.id == verseId } - 1)
        if (previous == null) {
            mergeIntoSectionAbove(verseId = verseId, text = text)
            return
        }
        viewModelScope.launch {
            editor.mergeVerses(previousId = previous.id, verseId = verseId)
            focus.value = VerseFocus(verseId = previous.id, text = previous.text + text, cursor = previous.text.length)
        }
    }

    /** Shows the lyrics at [size] sp in every song, kept between [MIN_FONT_SIZE] and [MAX_FONT_SIZE]. */
    fun onFontSizeChange(size: Int) {
        viewModelScope.launch { preferences.setEditorFontSize(size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE)) }
    }

    /** The field asked to take the focus has it. */
    fun onFocusHandled() {
        focus.value = null
    }

    // The syllables and rhymes of every section, in the song's language.
    private fun EditorUiState.withSounds(): EditorUiState {
        val engine = engineOf(language)
        return copy(
            sections = sections.map {
                it.copy(sounds = sectionSounds(verses = it.section.verses, engine = engine))
            },
        )
    }

    /**
     * The verse [verseId] has [syllables] for the user: the first word of the verse missing from the dictionaries
     * gets the syllables that make this count, and keeps them in every song.
     */
    fun onCorrectCount(verseId: String, syllables: Int) {
        val state = uiState.value
        val sound = state.sections.firstNotNullOfOrNull { it.sounds.verses[verseId] } ?: return
        val word = sound.count.guessedWords.firstOrNull() ?: return
        val now = engineOf(state.language).countSyllables(word).syllables
        val corrected = (now + syllables - sound.count.syllables).coerceAtLeast(1)
        viewModelScope.launch {
            corrections.correct(language = state.language, word = word, syllables = corrected)
        }
    }

    // The text of the song, each verse once: the repeats show their source's verses.
    private fun SongDocument.lyrics() =
        sections.filter { it.repeatOf == null }.flatMap { it.verses }.joinToString(separator = "\n") { it.text }

    private fun engineOf(language: SongLanguage) =
        engines.of(if (language == SongLanguage.ENGLISH) PhoneticLanguage.ENGLISH else PhoneticLanguage.FRENCH)

    // The first section showing the verse: the source of a repeat, or the repeat, hold the same verses.
    private fun sectionOf(verseId: String): SongSection? =
        uiState.value.sections.map { it.section }.firstOrNull { section -> section.verses.any { it.id == verseId } }

    // Backspace at the start of a section's first verse: the section merges into the one above, the verse keeping the
    // focus and its text.
    private fun mergeIntoSectionAbove(verseId: String, text: String) {
        val section = uiState.value.sections.firstOrNull { it.section.verses.firstOrNull()?.id == verseId } ?: return
        if (!section.mergeable) return
        onMergeSection(section.section.id)
        focus.value = VerseFocus(verseId = verseId, text = text, cursor = 0)
    }

    private fun verseText(verseId: String): String? = sectionOf(verseId)?.verses?.firstOrNull { it.id == verseId }?.text

    private fun String.singleLine() = replace('\n', ' ')

    @AssistedFactory
    interface Factory {
        fun create(songId: String): EditorViewModel
    }

    companion object {
        /** Smallest size of the lyrics, in sp. */
        const val MIN_FONT_SIZE = 14

        /** Largest size of the lyrics, in sp. */
        const val MAX_FONT_SIZE = 28

        private const val STOP_TIMEOUT_MILLIS = 5_000L
        private const val DETECTION_DELAY_MILLIS = 2_000L
        private const val VERSION_PAUSE_MILLIS = 2_000L
    }
}
