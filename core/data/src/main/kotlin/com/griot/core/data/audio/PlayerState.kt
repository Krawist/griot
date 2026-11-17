package com.griot.core.data.audio

/**
 * What the audio player plays.
 *
 * @property uri The address played or paused; `null` when nothing is loaded.
 * @property playing Whether it plays now.
 * @property positionMillis Where it is, in milliseconds.
 * @property durationMillis Its length in milliseconds; 0 while unknown.
 */
data class PlayerState(
    val uri: String? = null,
    val playing: Boolean = false,
    val positionMillis: Long = 0,
    val durationMillis: Long = 0,
)
