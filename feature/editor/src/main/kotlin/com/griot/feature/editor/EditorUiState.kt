package com.griot.feature.editor

import com.griot.core.data.model.ExportFile
import com.griot.core.data.model.Idea
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository

/**
 * What the editor shows.
 *
 * @property loading Whether the song is still being read.
 * @property missing Whether the song no longer exists or went to the trash: the editor closes.
 * @property title The song's title; empty for a song not named yet.
 * @property language Writing language chosen for the song: French, English or automatic.
 * @property detectedLanguage Dominant language of the lyrics, French or English; `null` while unknown.
 * @property writingLanguage The language the song is written in: the one chosen, or the one detected in automatic
 * mode; `null` while unknown. It picks the phonetic engine.
 * @property sections Sections in order, with their labels.
 * @property focus The verse to focus next, until its field took the focus; `null` otherwise.
 * @property fontSize Size of the lyrics, in sp, before the system font scale.
 * @property soundLanguage The language the syllables and rhymes are read in: [writingLanguage], else the language of
 * new songs, else French.
 * @property showRhymes Whether the rhymes are coloured.
 * @property export The song exported as Markdown, while its share sheet shows; `null` otherwise.
 * @property ideas The ideas linked to the song, newest first, which its « ⋯ » menu offers to add as verses.
 * @property editedVerse The verse being edited, whose history « Versions » opens; `null` when none is.
 * @property pasteUndo The last paste split into sections, while its undo bar shows; `null` otherwise.
 */
internal data class EditorUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String = "",
    val language: SongLanguage = SongLanguage.AUTOMATIC,
    val detectedLanguage: SongLanguage? = null,
    val writingLanguage: SongLanguage? = null,
    val sections: List<EditorSection> = emptyList(),
    val focus: VerseFocus? = null,
    val fontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    val soundLanguage: SongLanguage = SongLanguage.FRENCH,
    val showRhymes: Boolean = true,
    val pasteUndo: PasteUndo? = null,
    val editedVerse: String? = null,
    val export: ExportFile? = null,
    val ideas: List<Idea> = emptyList(),
)
