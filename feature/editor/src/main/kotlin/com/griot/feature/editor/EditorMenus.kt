package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

// The items of the song's menu: each one closes it, and opens its [onSheet] when it has one.
@Composable
internal fun SongMenu(
    uiState: EditorUiState,
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    onSheet: (EditorSheet) -> Unit,
    onOpenHistory: (String?) -> Unit,
) {
    SongMenuItems(
        language = stringResource(uiState.languageChoice.nameRes()),
        showRhymes = uiState.showRhymes,
        ideas = uiState.ideas,
        onVersions = {
            onClose()
            onOpenHistory(null)
        },
        onLanguage = { onSheet(EditorSheet.LANGUAGE) },
        onFontSize = { onSheet(EditorSheet.FONT_SIZE) },
        onShowRhymesChange = { show ->
            onClose()
            viewModel.onShowRhymesChange(show)
        },
        onExport = {
            onClose()
            viewModel.onExport()
        },
        onRename = { onSheet(EditorSheet.RENAME_SONG) },
        onAddIdea = { text ->
            onClose()
            viewModel.onAddIdea(text)
        },
    )
}

// The items of the menu of [section]: each one closes it, and opens its [onSheet] when it has one.
@Composable
internal fun SectionMenu(
    section: EditorSection,
    viewModel: EditorViewModel,
    onClose: () -> Unit,
    onSheet: (SectionSheet) -> Unit,
) {
    val id = section.section.id
    SectionMenuItems(
        section = section,
        onAddBelow = { onSheet(SectionSheet.ADD_BELOW) },
        onRename = { onSheet(SectionSheet.RENAME) },
        onRepeat = {
            onClose()
            viewModel.onRepeatSection(id)
        },
        onMove = { offset ->
            onClose()
            viewModel.onMoveSection(sectionId = id, offset = offset)
        },
        onMerge = {
            onClose()
            viewModel.onMergeSection(id)
        },
        onDelete = { onSheet(SectionSheet.DELETE) },
    )
}

// The [sheet] opened from the menu of [section], while both are set.
@Composable
internal fun OpenSectionSheet(
    section: EditorSection?,
    sheet: SectionSheet?,
    viewModel: EditorViewModel,
    onClosed: () -> Unit,
) {
    if (section == null || sheet == null) return
    SectionSheetContent(
        sheet = sheet,
        section = section,
        label = section.label.text(),
        onAddBelow = { kind ->
            viewModel.onAddSection(kind = kind, after = section.section.id)
            onClosed()
        },
        onRename = { name ->
            viewModel.onRenameSection(sectionId = section.section.id, name = name)
            onClosed()
        },
        onDelete = {
            viewModel.onDeleteSection(section.section.id)
            onClosed()
        },
        onDismiss = onClosed,
    )
}

/** The name of a choice of the language sheet: « Français », « English », « Automatique ». */
private fun LanguageChoice.nameRes(): Int = when (this) {
    LanguageChoice.FRENCH -> R.string.editor_language_french_name
    LanguageChoice.ENGLISH -> R.string.editor_language_english_name
    LanguageChoice.AUTOMATIC -> R.string.editor_language_automatic_name
}
