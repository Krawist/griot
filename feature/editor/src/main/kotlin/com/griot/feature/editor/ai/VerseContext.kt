package com.griot.feature.editor.ai

import com.griot.core.ai.VerseRequest
import com.griot.core.data.song.SectionLabel

/**
 * A verse of the song with its place, as the AI panel reads it.
 *
 * @property request The verse, its section and neighbours, as a model is asked about it.
 * @property section The label of its section.
 * @property number Its rank in its section, from 1.
 * @property ending The letters of its rhyming ending (« ume » of « bitume »); empty without a rhyme.
 * @property rhymes Words that rhyme with it, those of the song marked as used.
 */
internal data class VerseContext(
    val request: VerseRequest,
    val section: SectionLabel,
    val number: Int,
    val ending: String,
    val rhymes: List<RhymeChip>,
)
