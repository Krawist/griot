package com.griot.core.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
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

/** Default implementation for [AudioPlayer], on [MediaPlayer]. */
@Singleton
internal class AudioPlayerImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    AudioPlayer {
    private val mutableState = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var player: MediaPlayer? = null
    private var ticker: Job? = null

    override fun play(uri: String): Boolean {
        val current = player
        if (current != null && mutableState.value.uri == uri) {
            current.start()
            mutableState.value = mutableState.value.copy(playing = true)
            tick()
            return true
        }
        stop()
        val media = MediaPlayer()
        return try {
            media.setDataSource(context, Uri.parse(uri))
            media.prepare()
            media.setOnCompletionListener {
                mutableState.value = mutableState.value.copy(playing = false, positionMillis = 0)
                it.seekTo(0)
            }
            media.start()
            player = media
            mutableState.value = PlayerState(uri = uri, playing = true, durationMillis = media.duration.toLong())
            tick()
            true
        } catch (_: IOException) {
            media.release()
            false
        } catch (_: RuntimeException) {
            media.release()
            false
        }
    }

    override fun pause() {
        player?.pause()
        mutableState.value = mutableState.value.copy(playing = false)
    }

    override fun rewind(millis: Long) {
        val media = player ?: return
        val position = (media.currentPosition - millis).coerceAtLeast(0)
        media.seekTo(position.toInt())
        mutableState.value = mutableState.value.copy(positionMillis = position)
    }

    override fun stop() {
        ticker?.cancel()
        player?.release()
        player = null
        mutableState.value = PlayerState()
    }

    private fun tick() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val media = player ?: break
                mutableState.value = mutableState.value.copy(positionMillis = media.currentPosition.toLong())
                delay(TICK_MILLIS)
            }
        }
    }

    private companion object {
        const val TICK_MILLIS = 250L
    }
}
