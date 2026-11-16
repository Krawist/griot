package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import com.griot.core.design.format.RelativeTime

/**
 * One card of the library.
 *
 * @property song The song shown.
 * @property updated When the song last changed, as the card reads it.
 */
internal data class LibraryItem(val song: LibrarySong, val updated: RelativeTime)
