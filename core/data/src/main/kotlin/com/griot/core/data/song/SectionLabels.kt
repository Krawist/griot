package com.griot.core.data.song

import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongSection

/**
 * The labels of the [sections], in order. Verses are always numbered (« Couplet 1 », « Couplet 2 »); the other kinds
 * only when the song has several of them. A repeat takes the label of the section it repeats; a name given by the
 * author replaces the automatic label but keeps its rank.
 */
fun sectionLabels(sections: List<SongSection>): List<SectionLabel> {
    val originals = sections.filter { it.repeatOf == null }
    val counts = originals.groupingBy { it.kind }.eachCount()
    val ranks = mutableMapOf<SectionKind, Int>()
    val labels = originals.associate { section ->
        val rank = ranks.merge(section.kind, 1, Int::plus) ?: 1
        val numbered = section.kind == SectionKind.VERSE || counts.getValue(section.kind) > 1
        section.id to SectionLabel(kind = section.kind, number = rank.takeIf { numbered }, name = section.name)
    }
    return sections.map { section ->
        labels[section.repeatOf ?: section.id] ?: SectionLabel(kind = section.kind, number = null, name = section.name)
    }
}
