package com.griot.feature.library.folder

import com.griot.core.data.model.SongLanguage

/**
 * The filters of the folder screen.
 *
 * @property genre The id of the genre kept; `null` for every genre.
 * @property language The language kept; `null` for both.
 */
internal data class FolderFilters(val genre: String? = null, val language: SongLanguage? = null)
