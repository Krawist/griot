package com.griot.core.data.audio

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Default implementation for [ExternalMusic]: the media sessions of other apps with the notification access, the
 * system's media keys and whether music plays without it.
 */
@Singleton
internal class ExternalMusicImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val player: AudioPlayer,
) : ExternalMusic {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val sessions = context.getSystemService(MediaSessionManager::class.java)
    private val listener = ComponentName(context, MusicListener::class.java)

    // Without the access, the music Griot paused no longer plays, but stays the one to resume.
    @Volatile private var pausedByGriot = false

    // The cover and its colours, read once per picture.
    private var lastArtwork: Bitmap? = null
    private var lastCover: CoverArt? = null

    override fun observe(): Flow<ExternalMusicState> = flow {
        while (true) {
            emit(read())
            delay(TICK_MILLIS)
        }
    }.distinctUntilChanged().flowOn(Dispatchers.Default)

    override fun hasAccess(): Boolean =
        context.packageName in NotificationManagerCompat.getEnabledListenerPackages(context)

    override fun playPause() {
        val controller = controller()
        if (controller != null) {
            val playing = controller.playbackState?.state == PlaybackState.STATE_PLAYING
            if (playing) controller.transportControls.pause() else controller.transportControls.play()
            return
        }
        pausedByGriot = audio.isMusicActive
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        audio.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
    }

    override fun rewind(millis: Long) {
        val controller = controller() ?: return
        val position = controller.playbackState?.position ?: return
        controller.transportControls.seekTo((position - millis).coerceAtLeast(0))
    }

    private fun read(): ExternalMusicState {
        // Griot's own beat is not another app's music.
        val controller = if (player.state.value.playing) null else controller()
        if (controller == null) return if (player.state.value.playing) ownBeat() else withoutAccess()
        val state = controller.playbackState
        val metadata = controller.metadata
        return ExternalMusicState(
            active = true,
            controlled = true,
            app = appName(controller.packageName),
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            cover = coverOf(metadata),
            playing = state?.state == PlaybackState.STATE_PLAYING,
            positionMillis = state?.position?.coerceAtLeast(0) ?: 0,
            durationMillis = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0) ?: 0,
        )
    }

    private fun ownBeat(): ExternalMusicState {
        pausedByGriot = false
        return ExternalMusicState()
    }

    private fun withoutAccess(): ExternalMusicState {
        val playing = audio.isMusicActive
        if (playing) pausedByGriot = false
        return ExternalMusicState(active = playing || pausedByGriot, playing = playing)
    }

    // The session of another app that plays or is paused, the one playing first; `null` without the access.
    private fun controller(): MediaController? {
        val active = if (hasAccess()) {
            try {
                sessions.getActiveSessions(listener)
            } catch (_: SecurityException) {
                emptyList()
            }
        } else {
            emptyList()
        }
        val others = active.filter { it.packageName != context.packageName }
        return others.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: others.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PAUSED }
    }

    private fun coverOf(metadata: MediaMetadata?): CoverArt? {
        val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: return null
        if (artwork !== lastArtwork) {
            lastArtwork = artwork
            lastCover = coverArtOf(artwork)
        }
        return lastCover
    }

    private fun appName(packageName: String): String? = runCatching {
        val manager = context.packageManager
        manager.getApplicationLabel(manager.getApplicationInfo(packageName, 0)).toString()
    }.getOrNull()

    private companion object {
        const val TICK_MILLIS = 500L
    }
}
