package com.griot.core.data.model

/**
 * A section of a [SongDocument].
 *
 * @property id UUID of the section.
 * @property kind Intro, verse, chorus, bridge, outro or other.
 * @property name Name given by the author; `null` for the automatic label of its kind and rank.
 * @property repeatOf Id of the section it repeats; `null` for a section with its own verses.
 * @property verses Verses that are not deleted, in order: for a repeat, those of the section it repeats, so the
 * same verse ids show in both.
 */
data class SongSection(
    val id: String,
    val kind: SectionKind,
    val name: String?,
    val repeatOf: String?,
    val verses: List<SongVerse>,
)
