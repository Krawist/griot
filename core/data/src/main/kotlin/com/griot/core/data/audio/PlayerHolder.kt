package com.griot.core.data.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The one ExoPlayer of the app, shared by [AudioPlayerImpl] and the media session of [PlaybackService]. It takes the
 * audio focus (a call pauses it) and pauses when headphones are unplugged.
 */
@Singleton
internal class PlayerHolder @Inject constructor(@param:ApplicationContext context: Context) {
    val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(),
                true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}
