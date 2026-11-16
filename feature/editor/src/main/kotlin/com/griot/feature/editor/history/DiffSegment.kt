package com.griot.feature.editor.history

/**
 * A piece of a text in the history, changed or not compared with its neighbour version.
 *
 * @property text The piece, spaces included.
 * @property changed Whether it differs from the neighbour version: highlighted, and struck in an old version.
 */
internal data class DiffSegment(val text: String, val changed: Boolean)
