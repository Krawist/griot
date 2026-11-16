package com.griot.core.data.model

/**
 * A text to import as a song.
 *
 * @property name Name of the file without its extension, which titles the song; empty for a pasted text.
 * @property text The text; `null` when the file could not be read.
 */
data class ImportedText(val name: String, val text: String?)
