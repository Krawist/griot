package com.griot.feature.editor

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SectionLabel
import com.griot.core.design.component.FileShareSheet
import com.griot.core.design.component.FontSizeSheet
import com.griot.core.design.component.GriotSheet
import com.griot.core.design.component.TextInputForm
import com.griot.core.design.theme.GriotTheme
import com.griot.feature.editor.ai.AiPanelSheet
import com.griot.feature.editor.ai.AiTab
import com.griot.feature.editor.audio.BeatChoiceSheet
import com.griot.feature.editor.audio.BeatViewModel
import com.griot.feature.editor.audio.ExternalMusicSheet
import com.griot.feature.editor.audio.ExternalMusicViewModel
import com.griot.feature.editor.audio.MemoRecorder
import com.griot.feature.editor.audio.MemoRequest
import com.griot.feature.editor.audio.editorPlayer

/** The structured editor of the song [songId]: its title, its sections and their verses, all saved as typed. */
@Suppress("LongMethod") // The screen's menus and sheets, and how each one opens and closes.
@Composable
internal fun EditorScreen(
    songId: String,
    onBack: () -> Unit,
    onOpenHistory: (String?) -> Unit,
    onFile: () -> Unit,
    onOpenAudios: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel<EditorViewModel, EditorViewModel.Factory>(
        key = songId,
        creationCallback = { factory -> factory.create(songId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val beatViewModel = hiltViewModel<BeatViewModel, BeatViewModel.Factory>(
        key = "beat/$songId",
        creationCallback = { factory -> factory.create(songId) },
    )
    val beat by beatViewModel.uiState.collectAsStateWithLifecycle()
    val beatChoices by beatViewModel.choices.collectAsStateWithLifecycle()
    var choosingBeat by rememberSaveable { mutableStateOf(false) }
    val externalViewModel = hiltViewModel<ExternalMusicViewModel>()
    val external by externalViewModel.uiState.collectAsStateWithLifecycle()
    // A song deleted meanwhile, on this phone or another one, closes the editor.
    LaunchedEffect(uiState.missing) { if (uiState.missing) onBack() }
    var menu by remember { mutableStateOf<EditorMenu?>(null) }
    var sheet by rememberSaveable { mutableStateOf<EditorSheet?>(null) }
    var sectionSheet by rememberSaveable { mutableStateOf<SectionSheet?>(null) }
    var sheetSection by rememberSaveable { mutableStateOf<String?>(null) }
    var countFor by rememberSaveable { mutableStateOf<String?>(null) }
    var aiFor by rememberSaveable { mutableStateOf<String?>(null) }
    var aiTab by rememberSaveable { mutableStateOf(AiTab.RHYMES) }
    var verseSheetOpen by rememberSaveable { mutableStateOf(false) }
    var memo by rememberSaveable(stateSaver = MemoRequest.Saver) { mutableStateOf<MemoRequest?>(null) }
    val focusManager = LocalFocusManager.current
    // Another verse selected shows its sheet closed.
    LaunchedEffect(uiState.selection?.verseId) { verseSheetOpen = false }
    // Back closes the verse's options, then leaves the selection.
    BackHandler(enabled = uiState.selection != null) {
        if (verseSheetOpen) {
            verseSheetOpen = false
        } else {
            focusManager.clearFocus()
            viewModel.onVerseSelected(null)
        }
    }
    // An item of a menu closes it, and opens its sheet when it has one.
    val openSheet = { next: EditorSheet ->
        menu = null
        sheet = next
    }
    val openSectionSheet = { sectionId: String, next: SectionSheet ->
        menu = null
        sheetSection = sectionId
        sectionSheet = next
    }
    EditorContent(
        uiState = uiState,
        onBack = onBack,
        verseInput = viewModel.verseInput(onCountLongPress = { countFor = it }),
        verseSheet = { modifier, bottomInset ->
            uiState.selection?.let { selection ->
                VerseSheet(
                    selection = selection,
                    expanded = verseSheetOpen,
                    onExpandedChange = { verseSheetOpen = it },
                    onRhymes = {
                        aiTab = AiTab.RHYMES
                        aiFor = selection.verseId
                    },
                    onAiHelp = {
                        aiTab = AiTab.REPHRASE
                        aiFor = selection.verseId
                    },
                    onVersions = { onOpenHistory(selection.verseId) },
                    onMemo = { memo = MemoRequest(verseId = selection.verseId, onVerse = true) },
                    modifier = modifier,
                    bottomInset = bottomInset,
                )
            }
        },
        player = editorPlayer(
            beat = beat,
            choices = beatChoices.size,
            onChoose = { choosingBeat = true },
            beatViewModel = beatViewModel,
            external = external,
            externalViewModel = externalViewModel,
        ),
        menu = menu,
        onMenu = { menu = it },
        onOpenAudios = onOpenAudios,
        onRecord = { memo = MemoRequest(verseId = uiState.selection?.verseId) },
        songMenu = {
            SongMenu(
                uiState = uiState,
                viewModel = viewModel,
                onClose = { menu = null },
                onSheet = openSheet,
                onOpenHistory = onOpenHistory,
                onFile = onFile,
            )
        },
        sectionMenu = { section ->
            SectionMenu(
                section = section,
                viewModel = viewModel,
                onClose = { menu = null },
                onSheet = { next -> openSectionSheet(section.section.id, next) },
            )
        },
        onAddSection = { sheet = EditorSheet.ADD_SECTION },
        onFontSizeChange = viewModel::onFontSizeChange,
        onShowConflict = { uiState.conflicts.firstOrNull()?.let { onConflict(viewModel, it, onOpenHistory) } },
        onDismissConflicts = viewModel::onConflictsDismissed,
    )
    EditorSheets(
        uiState = uiState,
        viewModel = viewModel,
        sheet = sheet,
        onSheetClosed = { sheet = null },
        countFor = countFor,
        onCountClosed = { countFor = null },
    )
    OpenSectionSheet(
        section = uiState.sections.firstOrNull { it.section.id == sheetSection },
        sheet = sectionSheet,
        viewModel = viewModel,
        onClosed = {
            sectionSheet = null
            sheetSection = null
        },
    )
    MemoRecorder(songId = songId, request = memo, onClosed = { memo = null })
    if (choosingBeat && beatChoices.size > 1) {
        BeatChoiceSheet(
            files = beatChoices,
            current = beat?.id,
            onChoose = { id ->
                beatViewModel.onChoose(id)
                choosingBeat = false
            },
            onDetach = beatViewModel::onDetach,
            onDismiss = { choosingBeat = false },
        )
    }
    // The first time the player meets another app's music, without the access.
    if (beat == null && external.explain) ExternalMusicSheet(onDone = externalViewModel::onExplained)
    AiPanel(
        songId = songId,
        verseId = aiFor,
        tab = aiTab,
        uiState = uiState,
        viewModel = viewModel,
        onClosed = { aiFor = null },
    )
}

// How the verses are typed, into [EditorViewModel]; a long press on a count asks to correct it.
private fun EditorViewModel.verseInput(onCountLongPress: (String) -> Unit) = VerseInput(
    onChange = ::onVerseChange,
    onLines = ::onVerseLines,
    onBackspaceAtStart = ::onVerseBackspaceAtStart,
    onFocusHandled = ::onFocusHandled,
    onCountLongPress = onCountLongPress,
    onFocused = ::onVerseFocused,
    onSelect = ::onVerseSelected,
    onCursor = ::onVerseCursor,
)

// The notice of a conflict opens the history of its verse [verseId], and goes.
private fun onConflict(viewModel: EditorViewModel, verseId: String, onOpenHistory: (String?) -> Unit) {
    viewModel.onConflictShown(verseId)
    onOpenHistory(verseId)
}

// The AI panel of the verse [verseId] on [tab], while it is set; a rhyme or a suggestion chosen closes it.
@Suppress("LongParameterList") // The verse, its tab, the song, and how the panel closes.
@Composable
private fun AiPanel(
    songId: String,
    verseId: String?,
    tab: AiTab,
    uiState: EditorUiState,
    viewModel: EditorViewModel,
    onClosed: () -> Unit,
) {
    if (verseId == null) return
    AiPanelSheet(
        songId = songId,
        verseId = verseId,
        language = uiState.language,
        tab = tab,
        onInsertWord = { word ->
            onClosed()
            viewModel.onInsertWord(verseId = verseId, word = word)
        },
        onUseSuggestion = { text ->
            onClosed()
            viewModel.onUseSuggestion(verseId = verseId, text = text)
        },
        onDismiss = onClosed,
    )
}

// What the editor shows over its content: the undo bar of a paste, the [sheet] open, and the count to correct.
@Suppress("LongParameterList") // Which sheets are open, and how each one closes.
@Composable
private fun EditorSheets(
    uiState: EditorUiState,
    viewModel: EditorViewModel,
    sheet: EditorSheet?,
    onSheetClosed: () -> Unit,
    countFor: String?,
    onCountClosed: () -> Unit,
) {
    CountCorrection(uiState = uiState, viewModel = viewModel, verseId = countFor, onClosed = onCountClosed)
    PasteUndoOverlay(
        undo = uiState.pasteUndo,
        onUndo = viewModel::onUndoPaste,
        onDismissed = viewModel::onPasteUndoDismissed,
    )
    OpenSheet(sheet = sheet, uiState = uiState, viewModel = viewModel, onClosed = onSheetClosed)
}

// The sheet [sheet] of the editor, with its export's share sheet.
@Composable
private fun OpenSheet(sheet: EditorSheet?, uiState: EditorUiState, viewModel: EditorViewModel, onClosed: () -> Unit) {
    when (sheet) {
        EditorSheet.FONT_SIZE -> FontSizeSheet(
            size = uiState.fontSize,
            sizes = UserPreferencesRepository.EDITOR_FONT_SIZES,
            onSizeChange = viewModel::onFontSizeChange,
            onDismiss = onClosed,
        )
        EditorSheet.ADD_SECTION -> AddSectionSheet(
            onAdd = { kind ->
                viewModel.onAddSection(kind)
                onClosed()
            },
            onDismiss = onClosed,
        )
        EditorSheet.LANGUAGE -> LanguageSheet(
            choice = uiState.languageChoice,
            language = uiState.language,
            onSelect = { choice ->
                viewModel.onLanguageSelected(choice)
                onClosed()
            },
            onDismiss = onClosed,
        )
        EditorSheet.RENAME_SONG -> GriotSheet(onDismiss = onClosed) {
            TextInputForm(
                value = uiState.title,
                label = stringResource(R.string.editor_rename_song_label),
                placeholder = stringResource(R.string.editor_title_placeholder),
                confirmLabel = stringResource(R.string.editor_rename),
                onConfirm = { title ->
                    viewModel.onTitleChange(title)
                    onClosed()
                },
            )
        }
        null -> Unit
    }
    uiState.export?.let { file ->
        FileShareSheet(
            name = file.name,
            path = file.path,
            mimeType = file.mimeType,
            onSave = viewModel::onSaveExport,
            onDismiss = viewModel::onExportClosed,
        )
    }
}

// The count of the verse [verseId] to correct, when it holds a word the dictionaries do not know.
@Composable
private fun CountCorrection(
    uiState: EditorUiState,
    viewModel: EditorViewModel,
    verseId: String?,
    onClosed: () -> Unit,
) {
    val counted = uiState.sections.firstNotNullOfOrNull { it.sounds.verses[verseId] } ?: return
    val word = counted.count.guessedWords.firstOrNull() ?: return
    CountCorrectionSheet(
        word = word,
        count = counted.count.syllables,
        onSave = { syllables ->
            verseId?.let { viewModel.onCorrectCount(verseId = it, syllables = syllables) }
            onClosed()
        },
        onDismiss = onClosed,
    )
}

// Typing does nothing in the preview.
private val PreviewVerseInput = VerseInput(
    onChange = { _, _ -> },
    onLines = { _, _, _ -> },
    onBackspaceAtStart = { _, _ -> },
    onFocusHandled = {},
    onCountLongPress = {},
    onFocused = {},
    onSelect = {},
    onCursor = { _, _ -> },
)

@Preview(name = "Light", heightDp = 844, widthDp = 390)
@Preview(name = "Dark", heightDp = 844, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditorScreenPreview() {
    val verse = SongSection(
        id = "1",
        kind = SectionKind.VERSE,
        name = null,
        repeatOf = null,
        verses = listOf(
            SongVerse(id = "a", text = "Je marche seul dans la ville"),
            SongVerse(id = "b", text = "Les néons pleurent sur le bitume"),
        ),
    )
    val chorus = SongSection(
        id = "2",
        kind = SectionKind.CHORUS,
        name = null,
        repeatOf = null,
        verses = listOf(SongVerse(id = "c", text = "Et je chante, et je chante")),
    )
    GriotTheme {
        Box(Modifier.background(GriotTheme.palette.background)) {
            EditorContent(
                uiState = EditorUiState(
                    loading = false,
                    title = "Les néons",
                    language = SongLanguage.FRENCH,
                    sections = listOf(
                        EditorSection(
                            section = verse,
                            label = SectionLabel(SectionKind.VERSE, number = 1, name = null),
                            first = true,
                            last = false,
                        ),
                        EditorSection(
                            section = chorus,
                            label = SectionLabel(SectionKind.CHORUS, number = null, name = null),
                            first = false,
                            last = true,
                        ),
                    ),
                ),
                onBack = {},
                verseInput = PreviewVerseInput,
                menu = null,
                onMenu = {},
                onOpenAudios = {},
                onRecord = {},
                songMenu = {},
                sectionMenu = {},
                onAddSection = {},
                onFontSizeChange = {},
                onShowConflict = {},
                onDismissConflicts = {},
                verseSheet = { _, _ -> },
                player = null,
            )
        }
    }
}
