package com.griot.feature.editor

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.design.R as DesignR
import com.griot.core.design.theme.GriotRadius
import com.griot.core.design.theme.GriotSpacing
import com.griot.core.design.theme.GriotTheme

private val AddSectionHeight = 48.dp
private val DashWidth = 1.5.dp
private val SectionGap = 22.dp

/** The structured editor of the song [songId]: its title, its sections and their verses, all saved as typed. */
@Composable
internal fun EditorScreen(
    songId: String,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel<EditorViewModel, EditorViewModel.Factory>(
        key = songId,
        creationCallback = { factory -> factory.create(songId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // A song deleted meanwhile, on this phone or another one, closes the editor.
    LaunchedEffect(uiState.missing) { if (uiState.missing) onBack() }
    var sheet by rememberSaveable { mutableStateOf<EditorSheet?>(null) }
    var actionsFor by rememberSaveable { mutableStateOf<String?>(null) }
    var countFor by rememberSaveable { mutableStateOf<String?>(null) }
    EditorContent(
        uiState = uiState,
        onBack = onBack,
        onTitleChange = viewModel::onTitleChange,
        verseInput = VerseInput(
            onChange = viewModel::onVerseChange,
            onLines = viewModel::onVerseLines,
            onBackspaceAtStart = viewModel::onVerseBackspaceAtStart,
            onFocusHandled = viewModel::onFocusHandled,
            onCountLongPress = { countFor = it },
        ),
        onSectionActions = { actionsFor = it },
        onAddSection = { sheet = EditorSheet.ADD_SECTION },
        onFontSize = { sheet = EditorSheet.FONT_SIZE },
        onFontSizeChange = viewModel::onFontSizeChange,
        onLanguage = { sheet = EditorSheet.LANGUAGE },
        onShowRhymesChange = viewModel::onShowRhymesChange,
    )
    EditorSheets(
        uiState = uiState,
        viewModel = viewModel,
        sheet = sheet,
        onSheetClosed = { sheet = null },
        actionsFor = actionsFor,
        onActionsClosed = { actionsFor = null },
        countFor = countFor,
        onCountClosed = { countFor = null },
    )
}

// What the editor shows over its content: the undo bar of a paste, the [sheet] open, and the actions of a section.
@Suppress("LongParameterList") // Which sheets are open, and how each one closes.
@Composable
private fun EditorSheets(
    uiState: EditorUiState,
    viewModel: EditorViewModel,
    sheet: EditorSheet?,
    onSheetClosed: () -> Unit,
    actionsFor: String?,
    onActionsClosed: () -> Unit,
    countFor: String?,
    onCountClosed: () -> Unit,
) {
    CountCorrection(uiState = uiState, viewModel = viewModel, verseId = countFor, onClosed = onCountClosed)
    PasteUndoOverlay(
        undo = uiState.pasteUndo,
        onUndo = viewModel::onUndoPaste,
        onDismissed = viewModel::onPasteUndoDismissed,
    )
    when (sheet) {
        EditorSheet.FONT_SIZE -> FontSizeSheet(
            size = uiState.fontSize,
            onSizeChange = viewModel::onFontSizeChange,
            onDismiss = onSheetClosed,
        )
        EditorSheet.ADD_SECTION -> AddSectionSheet(
            onAdd = { kind ->
                viewModel.onAddSection(kind)
                onSheetClosed()
            },
            onDismiss = onSheetClosed,
        )
        EditorSheet.LANGUAGE -> LanguageSheet(
            language = uiState.language,
            onSelect = { language ->
                viewModel.onLanguageSelected(language)
                onSheetClosed()
            },
            onDismiss = onSheetClosed,
        )
        null -> Unit
    }
    uiState.sections.firstOrNull { it.section.id == actionsFor }?.let { section ->
        val close = onActionsClosed
        SectionActionsSheet(
            section = section,
            label = section.label.text(),
            onRename = { name ->
                viewModel.onRenameSection(sectionId = section.section.id, name = name)
                close()
            },
            onRepeat = {
                viewModel.onRepeatSection(section.section.id)
                close()
            },
            onMove = { offset ->
                viewModel.onMoveSection(sectionId = section.section.id, offset = offset)
                close()
            },
            onDelete = {
                viewModel.onDeleteSection(section.section.id)
                close()
            },
            onDismiss = close,
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

@Suppress("LongParameterList") // One callback per change the editor makes.
@Composable
private fun EditorContent(
    uiState: EditorUiState,
    onBack: () -> Unit,
    onTitleChange: (String) -> Unit,
    verseInput: VerseInput,
    onSectionActions: (String) -> Unit,
    onAddSection: () -> Unit,
    onFontSize: () -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onLanguage: () -> Unit,
    onShowRhymesChange: (Boolean) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorTopBar(
                title = uiState.title,
                language = uiState.language,
                detectedLanguage = uiState.detectedLanguage,
                onBack = onBack,
                onTitleChange = onTitleChange,
                onFontSize = onFontSize,
                onLanguage = onLanguage,
            )
            if (!uiState.loading) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pinchToResize(size = uiState.fontSize, onSizeChange = onFontSizeChange),
                    contentPadding = PaddingValues(
                        start = GriotSpacing.l,
                        top = GriotSpacing.xl,
                        end = GriotSpacing.l,
                        bottom = GriotSpacing.xxl + EditorToolbarHeight + GriotSpacing.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(SectionGap),
                ) {
                    items(items = uiState.sections, key = { it.section.id }) { section ->
                        SectionBlock(
                            section = section,
                            fontSize = uiState.fontSize,
                            showRhymes = uiState.showRhymes,
                            focus = uiState.focus,
                            verseInput = verseInput,
                            onActions = { onSectionActions(section.section.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                    item(key = "add") { AddSectionButton(onClick = onAddSection, modifier = Modifier.animateItem()) }
                }
            }
        }
        EditorToolbar(
            showRhymes = uiState.showRhymes,
            onAddSection = onAddSection,
            onShowRhymesChange = onShowRhymesChange,
            modifier = Modifier.align(Alignment.BottomCenter).padding(GriotSpacing.l),
        )
    }
}

@Composable
private fun AddSectionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = GriotTheme.palette
    val shape = RoundedCornerShape(GriotRadius.field)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(AddSectionHeight)
            .clip(shape)
            .drawBehind {
                val radius = GriotRadius.field.toPx()
                drawRoundRect(
                    color = palette.line,
                    cornerRadius = CornerRadius(radius),
                    style = Stroke(
                        width = DashWidth.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
                    ),
                )
            }
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(DesignR.drawable.ic_add),
            contentDescription = null,
            tint = palette.muted,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = stringResource(R.string.editor_add_section),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = palette.muted,
            modifier = Modifier.padding(start = GriotSpacing.s),
        )
    }
}

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
                onTitleChange = {},
                verseInput = VerseInput(onChange = { _, _ ->
                }, onLines = { _, _, _ ->
                }, onBackspaceAtStart = { _, _ -> }, onFocusHandled = {}, onCountLongPress = {}),
                onSectionActions = {},
                onAddSection = {},
                onFontSize = {},
                onFontSizeChange = {},
                onLanguage = {},
                onShowRhymesChange = {},
            )
        }
    }
}
