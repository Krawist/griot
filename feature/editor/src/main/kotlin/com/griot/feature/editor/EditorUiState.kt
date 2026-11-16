package com.griot.feature.editor

import com.griot.core.data.model.SongLanguage

/**
 * What the editor shows.
 *
 * @property loading Whether the song is still being read.
 * @property missing Whether the song no longer exists or went to the trash: the editor closes.
 * @property title The song's title; empty for a song not named yet.
 * @property language Writing language, shown in the top bar.
 * @property sections Sections in order, with their labels.
 * @property focus The verse to focus next, until its field took the focus; `null` otherwise.
 */
internal data class EditorUiState(
    val loading: Boolean = true,
    val missing: Boolean = false,
    val title: String = "",
    val language: SongLanguage = SongLanguage.AUTOMATIC,
    val sections: List<EditorSection> = emptyList(),
    val focus: VerseFocus? = null,
)
