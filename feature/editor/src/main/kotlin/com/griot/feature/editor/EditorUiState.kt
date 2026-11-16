package com.griot.feature.editor

import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository

/**
 * What the editor shows.
 *
 * @property loading Whether the song is still being read.
 * @property missing Whether the song no longer exists or went to the trash: the editor closes.
 * @property title The song's title; empty for a song not named yet.
 * @property language Writing language, shown in the top bar.
 * @property sections Sections in order, with their labels.
 * @property focus The verse to focus next, until its field took the focus; `null` otherwise.
 * @property fontSize Size of the lyrics, in sp, before the system font scale.
 * @property pasteUndo The last paste split into sections, while its undo bar shows; `null` otherwise.
 */
internal data class EditorUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String = "",
    val language: SongLanguage = SongLanguage.AUTOMATIC,
    val sections: List<EditorSection> = emptyList(),
    val focus: VerseFocus? = null,
    val fontSize: Int = UserPreferencesRepository.DEFAULT_EDITOR_FONT_SIZE,
    val pasteUndo: PasteUndo? = null,
)
