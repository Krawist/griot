package com.griot.core.data.model

/**
 * A section to create from a pasted text.
 *
 * @property kind Kind of the section.
 * @property name Name to give it; `null` for the automatic label.
 * @property lines Its verses; for a repeat, ignored (it shows the verses of the section it repeats).
 * @property repeatOf Index, in the same list, of the section it repeats; `null` for a section with its own verses.
 */
data class NewSection(val kind: SectionKind, val name: String?, val lines: List<String>, val repeatOf: Int?)
