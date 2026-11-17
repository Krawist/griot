package com.griot.feature.editor.audio

import com.griot.core.data.song.SectionLabel

/**
 * What a memo can be attached to: the whole song, a section, or a verse.
 *
 * @property sectionId The section; `null` for the whole song.
 * @property verseId The verse; `null` for a section or the whole song.
 * @property label The section's label (the verse's section for a verse); `null` for the whole song.
 * @property verseNumber The verse's place in its section, from 1; `null` for a section or the whole song.
 */
internal data class MemoTarget(
    val sectionId: String? = null,
    val verseId: String? = null,
    val label: SectionLabel? = null,
    val verseNumber: Int? = null,
)
