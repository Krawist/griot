package com.griot.core.data.audio

/**
 * What Griot reads of an audio file of the phone.
 *
 * @property title Its name without its extension.
 * @property durationMillis Its length in milliseconds; `null` when it cannot be read.
 */
data class AudioFileInfo(val title: String, val durationMillis: Long?)
