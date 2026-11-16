package com.griot.core.phonetics.paste

/**
 * One section found in a pasted text.
 *
 * @property kind Kind given by a label, or [BlockKind.CHORUS] for a block sung again later; `null` for a plain block.
 * @property label The label's word as written (« Refrain », « Pre-chorus »), without brackets, number or colon;
 * `null` without a label.
 * @property lines The verses, without the label nor the blank lines; empty for a repeat named by a lone label.
 * @property repeatOf Index, in the list, of the block this one repeats; `null` for a block of its own.
 */
data class PastedBlock(val kind: BlockKind?, val label: String?, val lines: List<String>, val repeatOf: Int?)
