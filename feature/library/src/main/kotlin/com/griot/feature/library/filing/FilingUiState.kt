package com.griot.feature.library.filing

/**
 * What the « Dossier et genre » sheet shows.
 *
 * @property title The song's title, empty for a song not named; `null` when several songs are filed.
 * @property songCount How many songs are filed.
 * @property folders Every folder, « Archives » last, with whether the songs are in it.
 * @property genres Every genre, with whether the songs have it: a song can have several.
 * @property loading Whether the songs are still being read.
 */
internal data class FilingUiState(
    val title: String? = null,
    val songCount: Int = 0,
    val folders: List<FilingFolder> = emptyList(),
    val genres: List<FilingGenre> = emptyList(),
    val loading: Boolean = true,
)
