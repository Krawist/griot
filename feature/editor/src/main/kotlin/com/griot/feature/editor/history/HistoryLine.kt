package com.griot.feature.editor.history

import com.griot.feature.editor.SectionLabel

/**
 * A line of a version: a section's label, or a verse.
 *
 * @property label The label of the section starting here; `null` for a verse.
 * @property segments The verse, its changed words marked; empty for a label.
 */
internal data class HistoryLine(val label: SectionLabel?, val segments: List<DiffSegment>)
