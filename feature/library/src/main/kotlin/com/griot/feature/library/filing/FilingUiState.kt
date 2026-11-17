package com.griot.feature.library.filing

import com.griot.core.data.model.Genre

/**
 * What the « Dossier et genre » sheet shows.
 *
 * @property title The song's title, empty for a song not named; `null` when several songs are filed.
 * @property songCount How many songs are filed.
 * @property folders Every folder, « Archives » last, with whether the songs are in it.
 * @property genres Every genre.
 * @property genre The id of the genre chosen; `null` for none, or while the songs' genres differ and none was tapped.
 * @property loading Whether the songs are still being read.
 */
internal data class FilingUiState(
    val title: String? = null,
    val songCount: Int = 0,
    val folders: List<FilingFolder> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val genre: String? = null,
    val loading: Boolean = true,
)
