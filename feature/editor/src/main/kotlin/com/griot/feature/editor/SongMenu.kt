package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.griot.core.data.model.Idea
import com.griot.core.design.component.GriotMenuItem
import com.griot.core.design.component.GriotMenuLabel

/**
 * The items of the song's « ⋯ » menu: the history, its folders and genre, the language, the size of the text, the
 * rhymes shown or not, the export, the title, then the ideas linked to the song, which a tap adds as a verse.
 *
 * @param language The language as the sheet would show it chosen: « Français », « English » or « Automatique ».
 */
@Suppress("LongParameterList") // The song's state and one callback per item.
@Composable
internal fun SongMenuItems(
    language: String,
    showRhymes: Boolean,
    ideas: List<Idea>,
    onVersions: () -> Unit,
    onLanguage: () -> Unit,
    onFontSize: () -> Unit,
    onShowRhymesChange: (Boolean) -> Unit,
    onExport: () -> Unit,
    onRename: () -> Unit,
    onFile: () -> Unit,
    onAddIdea: (String) -> Unit,
) {
    GriotMenuItem(label = stringResource(R.string.editor_menu_versions), onClick = onVersions)
    GriotMenuItem(label = stringResource(R.string.editor_menu_file), onClick = onFile)
    GriotMenuItem(label = stringResource(R.string.editor_menu_language, language), onClick = onLanguage)
    GriotMenuItem(label = stringResource(R.string.editor_font_size), onClick = onFontSize)
    GriotMenuItem(
        label = stringResource(if (showRhymes) R.string.editor_menu_hide_rhymes else R.string.editor_menu_show_rhymes),
        onClick = { onShowRhymesChange(!showRhymes) },
    )
    GriotMenuItem(label = stringResource(R.string.editor_export), onClick = onExport)
    GriotMenuItem(label = stringResource(R.string.editor_menu_rename_song), onClick = onRename)
    val texts = ideas.filter { it.text.isNotBlank() }
    if (texts.isNotEmpty()) {
        GriotMenuLabel(text = stringResource(R.string.editor_ideas))
        texts.forEach { idea -> GriotMenuItem(label = idea.text, onClick = { onAddIdea(idea.text) }) }
    }
}
