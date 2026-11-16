package com.griot.feature.editor

import com.griot.core.data.model.PastedSections

/**
 * A paste split into sections, which the undo bar can turn back into one section.
 *
 * @property verseId The verse pasted into.
 * @property lines The text as pasted, one verse per line, blank lines left out.
 * @property paste What the paste added.
 * @property sectionCount Number of sections the text was split into, for the undo bar.
 */
internal data class PasteUndo(
    val verseId: String,
    val lines: List<String>,
    val paste: PastedSections,
    val sectionCount: Int,
)
