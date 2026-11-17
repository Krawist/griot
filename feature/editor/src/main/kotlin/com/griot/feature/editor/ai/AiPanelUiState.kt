package com.griot.feature.editor.ai

import com.griot.core.ai.AiLocation
import com.griot.core.ai.CheckedSuggestion
import com.griot.core.data.song.SectionLabel

/**
 * What the AI panel shows for one verse.
 *
 * @property loading Whether the verse is still being read.
 * @property verse The verse as written.
 * @property section The label of its section; `null` while loading.
 * @property number Its rank in its section, from 1.
 * @property syllables Its syllables, from the phonetic engine.
 * @property ending The letters of its rhyming ending (« ume » of « bitume »); empty without a rhyme.
 * @property tab The tab shown.
 * @property rhymes Words that rhyme with it, by number of syllables.
 * @property rephrase The versions proposed by a model.
 * @property critique The remarks of a model.
 * @property location Where the model answering runs; `null` when no model is available.
 */
internal data class AiPanelUiState(
    val loading: Boolean = true,
    val verse: String = "",
    val section: SectionLabel? = null,
    val number: Int = 0,
    val syllables: Int = 0,
    val ending: String = "",
    val tab: AiTab = AiTab.RHYMES,
    val rhymes: List<RhymeChip> = emptyList(),
    val rephrase: AiContent<CheckedSuggestion> = AiContent(),
    val critique: AiContent<String> = AiContent(),
    val location: AiLocation? = null,
)
