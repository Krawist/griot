package com.griot.feature.editor

/**
 * The verse that takes the focus after a split, a merge or a new section, with what its field shows.
 *
 * @property verseId UUID of the verse.
 * @property text Its text, already saved: the field shows it at once, before the song is read again.
 * @property cursor Where the cursor goes in [text], from 0.
 */
internal data class VerseFocus(val verseId: String, val text: String, val cursor: Int)
