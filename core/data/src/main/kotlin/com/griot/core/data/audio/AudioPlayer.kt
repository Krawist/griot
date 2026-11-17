package com.griot.core.data.audio

import kotlinx.coroutines.flow.StateFlow

/**
 * Plays one audio of the phone at a time, a beat or a memo, with a media session: it keeps playing while the author
 * writes, shows on the lock screen and in the notification shade, and pauses for a call.
 */
interface AudioPlayer {
    /** What plays, updated a few times a second while it runs. */
    val state: StateFlow<PlayerState>

    /**
     * Plays [uri], named [title] on the lock screen, from its start, or goes on when it is the one paused; `false`
     * when it cannot be read.
     */
    fun play(uri: String, title: String): Boolean

    fun pause()

    /** Goes back by [millis], never before the start. */
    fun rewind(millis: Long)

    /** Stops and forgets what played. */
    fun stop()
}
