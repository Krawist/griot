package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import com.griot.core.design.format.RelativeTime

/**
 * One card of the library.
 *
 * @property song The song shown.
 * @property updated When the song last changed, as the card reads it.
 * @property rhymes The rhyme ranks of the verses of its first section that have a rhyme, in order (0 for A): its
 * dots, coloured as in the editor.
 */
internal data class LibraryItem(val song: LibrarySong, val updated: RelativeTime, val rhymes: List<Int> = emptyList())
