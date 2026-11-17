package com.griot.core.data.audio

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** The media session of the beat: its controls on the lock screen and in the notification shade. */
@AndroidEntryPoint
internal class PlaybackService : MediaSessionService() {
    @Inject
    lateinit var holder: PlayerHolder

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        // Added at once: the notification and the lock screen show it without waiting for a controller.
        session = MediaSession.Builder(this, holder.player).build().also(::addSession)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    // Swiped away from the recent apps while paused: nothing is left to control.
    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        if (!holder.player.playWhenReady) stopSelf()
    }

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }
}
