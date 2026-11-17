package com.griot.feature.editor

import com.griot.core.data.song.SectionLabel

/**
 * The selected verse, as the summary of its sheet shows it.
 *
 * @property verseId The verse's id.
 * @property section The label of its section (the source's, for a verse shown in a repeat).
 * @property number Its place in the section, from 1.
 * @property syllables Its count; `null` while it has no text.
 * @property letter Its rhyme letter's index (A = 0); `null` when it has none or the rhymes are hidden.
 */
internal data class VerseSelection(
    val verseId: String,
    val section: SectionLabel,
    val number: Int,
    val syllables: Int?,
    val letter: Int?,
)
