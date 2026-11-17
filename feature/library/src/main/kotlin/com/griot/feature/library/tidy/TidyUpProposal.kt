package com.griot.feature.library.tidy

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.TidyUpKind

/**
 * One proposal of the tidy-up, accepted or refused on its own.
 *
 * @property id `kind:key`, unique among the proposals.
 * @property kind What it proposes.
 * @property songIds The songs it changes; for duplicates, the one kept first.
 * @property songTitle The title of its (first) song; empty for a song not named.
 * @property firstVerse The first verse of its song, for a title proposed; `null` otherwise.
 * @property title The title proposed; `null` for the other kinds.
 * @property folder The folder proposed; `null` for none, or for the other kinds.
 * @property genre The genre proposed; `null` for none, or for the other kinds.
 * @property decision What the author decided; `null` while it waits.
 */
internal data class TidyUpProposal(
    val id: String,
    val kind: TidyUpKind,
    val songIds: List<String>,
    val songTitle: String,
    val firstVerse: String? = null,
    val title: String? = null,
    val folder: Folder? = null,
    val genre: Genre? = null,
    val decision: TidyUpDecision? = null,
)
