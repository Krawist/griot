package com.griot.feature.editor

import com.griot.core.data.model.SongLanguage

/**
 * What the « Langue de la chanson » sheet shows.
 *
 * @property choice The mode of the song: automatic, or the language set by hand.
 * @property language The language of the song, the one detected today in the automatic mode.
 * @property loading Whether the song is still being read.
 */
internal data class SongLanguageUiState(
    val choice: LanguageChoice = LanguageChoice.FRENCH,
    val language: SongLanguage = SongLanguage.FRENCH,
    val loading: Boolean = true,
)
