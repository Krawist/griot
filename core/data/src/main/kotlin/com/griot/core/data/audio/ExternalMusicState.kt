package com.griot.core.data.audio

/**
 * The music another app plays (Spotify, YouTube, Audiomack…), as Griot sees it.
 *
 * @property active Whether another app plays music, or Griot just paused it.
 * @property controlled Whether Griot reads its media session (notification access): title, cover, progress and the
 * 15 seconds back. Without it, only play and pause, through the media keys.
 * @property app The name of the app that plays; `null` without the access.
 * @property title The title played; `null` without the access or when the app gives none.
 * @property artist Its artist; `null` when unknown.
 * @property cover Its cover and the player's colours; `null` without one.
 * @property playing Whether it plays now.
 * @property positionMillis Where it is, in milliseconds; 0 without the access.
 * @property durationMillis Its length in milliseconds; 0 when unknown.
 */
data class ExternalMusicState(
    val active: Boolean = false,
    val controlled: Boolean = false,
    val app: String? = null,
    val title: String? = null,
    val artist: String? = null,
    val cover: CoverArt? = null,
    val playing: Boolean = false,
    val positionMillis: Long = 0,
    val durationMillis: Long = 0,
)
