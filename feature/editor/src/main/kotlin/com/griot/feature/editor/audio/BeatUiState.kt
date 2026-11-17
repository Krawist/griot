package com.griot.feature.editor.audio

import com.griot.core.data.audio.CoverArt

/**
 * The beat of the song, as the player under the lyrics shows it.
 *
 * @property title The beat's name.
 * @property uri The address of its file.
 * @property cover Its cover and the player's colours; `null` while they are read.
 * @property playing Whether it plays now.
 * @property positionMillis Where it is, in milliseconds.
 * @property durationMillis Its length in milliseconds.
 */
internal data class BeatUiState(
    val title: String,
    val uri: String,
    val cover: CoverArt? = null,
    val playing: Boolean = false,
    val positionMillis: Long = 0,
    val durationMillis: Long = 0,
)
