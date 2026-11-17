package com.griot.core.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.UUID
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

/** Default implementation for [VoiceRecorder], on [MediaRecorder]: AAC at 128 kbit/s in `files/memos/`. */
@Singleton
internal class VoiceRecorderImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    VoiceRecorder {
    private val mutableState = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = mutableState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var ticker: Job? = null

    // The time recorded before the last resume, and when the recording last started running.
    private var recordedBefore = 0L
    private var runningSince = 0L

    override fun start(): Boolean {
        if (recorder != null) return true
        val memo = File(context.filesDir, "memos").apply { mkdirs() }.resolve("${UUID.randomUUID()}.m4a")
        val media = MediaRecorder(context)
        return try {
            media.setAudioSource(MediaRecorder.AudioSource.MIC)
            media.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            media.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            media.setAudioEncodingBitRate(BIT_RATE)
            media.setAudioSamplingRate(SAMPLING_RATE)
            media.setOutputFile(memo)
            media.prepare()
            media.start()
            recorder = media
            file = memo
            recordedBefore = 0
            runningSince = SystemClock.elapsedRealtime()
            mutableState.value = RecorderState(recording = true)
            tick()
            true
        } catch (_: IOException) {
            media.release()
            memo.delete()
            false
        } catch (_: RuntimeException) {
            media.release()
            memo.delete()
            false
        }
    }

    override fun pause() {
        val media = recorder ?: return
        if (mutableState.value.paused) return
        media.pause()
        recordedBefore += SystemClock.elapsedRealtime() - runningSince
        mutableState.value = mutableState.value.copy(paused = true, elapsedMillis = recordedBefore, level = 0f)
    }

    override fun resume() {
        val media = recorder ?: return
        if (!mutableState.value.paused) return
        media.resume()
        runningSince = SystemClock.elapsedRealtime()
        mutableState.value = mutableState.value.copy(paused = false)
    }

    override fun stop(): RecordedMemo? {
        val memo = file ?: return null
        val duration = elapsed()
        val kept = try {
            recorder?.stop()
            true
        } catch (_: RuntimeException) {
            // Stopped too early: nothing was written.
            false
        }
        release()
        if (!kept) memo.delete()
        return if (kept) RecordedMemo(uri = Uri.fromFile(memo).toString(), durationMillis = duration) else null
    }

    override fun cancel() {
        runCatching { recorder?.stop() }
        file?.delete()
        release()
    }

    private fun release() {
        ticker?.cancel()
        recorder?.release()
        recorder = null
        file = null
        mutableState.value = RecorderState()
    }

    private fun elapsed(): Long =
        if (mutableState.value.paused) recordedBefore else recordedBefore + SystemClock.elapsedRealtime() - runningSince

    // The time and the loudness, about ten times a second.
    private fun tick() {
        ticker = scope.launch {
            while (isActive) {
                val media = recorder ?: break
                if (!mutableState.value.paused) {
                    val level = (media.maxAmplitude.toFloat() / MAX_AMPLITUDE).coerceIn(0f, 1f)
                    mutableState.value = mutableState.value.copy(elapsedMillis = elapsed(), level = level)
                }
                delay(TICK_MILLIS)
            }
        }
    }

    private companion object {
        const val BIT_RATE = 128_000
        const val SAMPLING_RATE = 44_100
        const val MAX_AMPLITUDE = 32_767f
        const val TICK_MILLIS = 100L
    }
}
