package com.griot.core.data.audio

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Default implementation for [AudioPlayer], on Media3's ExoPlayer, with the session of [PlaybackService]. */
@Singleton
internal class AudioPlayerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val holder: PlayerHolder,
) : AudioPlayer {
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var ticker: Job? = null
    private var listening = false

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            update()
            if (isPlaying) tick() else ticker?.cancel()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                holder.player.pause()
                holder.player.seekTo(0)
            }
            update()
        }

        override fun onPlayerError(error: PlaybackException) {
            stop()
        }
    }

    override fun play(uri: String, title: String): Boolean {
        val player = holder.player
        if (!listening) {
            player.addListener(listener)
            listening = true
        }
        if (mutableState.value.uri != uri) {
            val parsed = runCatching { Uri.parse(uri) }.getOrNull() ?: return false
            val item = MediaItem.Builder()
                .setUri(parsed)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
                .build()
            player.setMediaItem(item)
            player.prepare()
            mutableState.value = PlayerState(uri = uri)
        }
        // The session shows on the lock screen while it plays.
        context.startService(Intent(context, PlaybackService::class.java))
        player.play()
        return true
    }

    override fun pause() {
        holder.player.pause()
    }

    override fun rewind(millis: Long) {
        val player = holder.player
        player.seekTo((player.currentPosition - millis).coerceAtLeast(0))
        update()
    }

    override fun stop() {
        ticker?.cancel()
        holder.player.stop()
        holder.player.clearMediaItems()
        mutableState.value = PlayerState()
    }

    private fun update() {
        val player = holder.player
        if (mutableState.value.uri == null) return
        mutableState.value = mutableState.value.copy(
            playing = player.isPlaying,
            positionMillis = player.currentPosition,
            durationMillis = player.duration.coerceAtLeast(0),
        )
    }

    private fun tick() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                update()
                delay(TICK_MILLIS)
            }
        }
    }

    private companion object {
        const val TICK_MILLIS = 250L
    }
}
