package com.griot.core.data.audio

/**
 * A voice memo just recorded, in the app's files.
 *
 * @property uri The `file://` address of the memo (AAC in an MP4 box).
 * @property durationMillis Its length in milliseconds.
 */
data class RecordedMemo(val uri: String, val durationMillis: Long)
