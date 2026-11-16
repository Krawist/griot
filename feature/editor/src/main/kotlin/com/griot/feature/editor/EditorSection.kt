package com.griot.feature.editor

import com.griot.core.data.model.SongSection

/**
 * One section of the editor.
 *
 * @property section The section and its verses.
 * @property label Its label, before it is put in words.
 * @property first Whether it is the first section: it cannot move up.
 * @property last Whether it is the last section: it cannot move down.
 */
internal data class EditorSection(
    val section: SongSection,
    val label: SectionLabel,
    val first: Boolean,
    val last: Boolean,
)
