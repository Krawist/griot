package com.griot.feature.library

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.design.format.RelativeTime

/**
 * One card of a song.
 *
 * @property song The song shown.
 * @property updated When the song last changed, as the card reads it.
 * @property rhymes The rhyme ranks of the verses of its first section that have a rhyme, in order (0 for A): its
 * dots, coloured as in the editor.
 * @property folder The folder it is in; `null` for « Sans dossier ».
 * @property genre Its genre; `null` without one.
 */
internal data class LibraryItem(
    val song: LibrarySong,
    val updated: RelativeTime,
    val rhymes: List<Int> = emptyList(),
    val folder: Folder? = null,
    val genre: Genre? = null,
)
