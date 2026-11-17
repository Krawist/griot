package com.griot.core.data.audio

import kotlinx.coroutines.flow.Flow

/** The music of other apps, which Griot's player controls (GRI-53). */
interface ExternalMusic {
    /** What another app plays, a few times a second while it is collected. */
    fun observe(): Flow<ExternalMusicState>

    /** Whether Griot has the notification access, which shows the title, the cover and the progress. */
    fun hasAccess(): Boolean

    /** Plays or pauses the music of the other app. */
    fun playPause()

    /** Goes back by [millis], never before the start; only with the access. */
    fun rewind(millis: Long)
}
