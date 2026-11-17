package com.griot.feature.editor.audio

/**
 * What the audios screen of a song shows.
 *
 * @property songTitle The song's title; empty for a song not named.
 * @property beats Files and links, newest first: « Beats et musique ».
 * @property memos Voice memos, newest first: « Mémos vocaux ».
 * @property loading Whether the audios are still being read.
 */
internal data class AudiosUiState(
    val songTitle: String = "",
    val beats: List<AudioItem> = emptyList(),
    val memos: List<AudioItem> = emptyList(),
    val loading: Boolean = true,
)
