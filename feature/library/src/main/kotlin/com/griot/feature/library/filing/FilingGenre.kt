package com.griot.feature.library.filing

import com.griot.core.data.model.Genre

/**
 * A genre of the sheet, with whether the songs have it.
 *
 * @property genre The genre.
 * @property check Whether the songs have it, with the author's taps.
 */
internal data class FilingGenre(val genre: Genre, val check: FilingCheck)
