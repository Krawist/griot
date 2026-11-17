package com.griot.feature.editor.audio

import com.griot.core.data.audio.ExternalMusicState

/**
 * The music of another app as the editor shows it.
 *
 * @property music What plays, and how much Griot sees of it.
 * @property explain Whether to show « Contrôler la musique des autres apps » now: the first time the player meets
 * music of another app without the access.
 */
internal data class ExternalMusicUiState(
    val music: ExternalMusicState = ExternalMusicState(),
    val explain: Boolean = false,
)
