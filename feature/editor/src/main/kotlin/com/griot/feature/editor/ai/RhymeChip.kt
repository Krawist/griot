package com.griot.feature.editor.ai

/**
 * A word of the « Rimes » tab.
 *
 * @property word The word, in lower case.
 * @property syllables Its syllables.
 * @property used Whether the song already has it: it is shown crossed out.
 */
internal data class RhymeChip(val word: String, val syllables: Int, val used: Boolean)
