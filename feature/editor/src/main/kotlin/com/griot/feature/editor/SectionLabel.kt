package com.griot.feature.editor

import com.griot.core.data.model.SectionKind

/**
 * The label of a section, before it is put in words: « Couplet 2 », « Refrain », or the name given by the author.
 *
 * @property kind Kind of the section, which names it.
 * @property number Rank among the sections of its kind; `null` when the kind is not numbered.
 * @property name Name given by the author, shown instead; `null` for the automatic label.
 */
internal data class SectionLabel(val kind: SectionKind, val number: Int?, val name: String?)
