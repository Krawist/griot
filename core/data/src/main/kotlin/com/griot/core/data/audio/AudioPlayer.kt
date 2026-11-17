package com.griot.core.data.audio

import kotlinx.coroutines.flow.StateFlow

/** Plays one audio of the phone at a time: a memo or a file. */
interface AudioPlayer {
    /** What plays, updated a few times a second while it runs. */
    val state: StateFlow<PlayerState>

    /** Plays [uri] from its start, or goes on when it is the one paused; `false` when it cannot be read. */
    fun play(uri: String): Boolean

    fun pause()

    /** Goes back by [millis], never before the start. */
    fun rewind(millis: Long)

    /** Stops and forgets what played. */
    fun stop()
}
