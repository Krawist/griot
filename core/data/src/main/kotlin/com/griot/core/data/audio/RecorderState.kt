package com.griot.core.data.audio

/**
 * Where a voice memo's recording is.
 *
 * @property recording Whether a memo is being recorded, paused or not.
 * @property paused Whether the recording is paused.
 * @property elapsedMillis How long has been recorded, pauses left out.
 * @property level The loudness heard last, from 0 (silence) to 1.
 */
data class RecorderState(
    val recording: Boolean = false,
    val paused: Boolean = false,
    val elapsedMillis: Long = 0,
    val level: Float = 0f,
)
