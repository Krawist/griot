package com.griot.feature.library

/**
 * The last song touched, which « Reprendre » offers when nothing changed in the last 24 hours.
 *
 * @property item The song, with when it last changed.
 * @property verses Its first two verses with text, at most.
 */
internal data class ResumeSong(val item: LibraryItem, val verses: List<String>)
