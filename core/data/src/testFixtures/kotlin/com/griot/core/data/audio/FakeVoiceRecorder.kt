package com.griot.core.data.audio

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [VoiceRecorder] for tests: it records unless [available] is `false`, and [stop] gives [memo]; [calls] records what
 * was asked, such as `"start"`, in order.
 */
class FakeVoiceRecorder(
    private val available: Boolean = true,
    private val memo: RecordedMemo? = RecordedMemo(uri = "my-uri", durationMillis = 1_000),
) : VoiceRecorder {
    override val state = MutableStateFlow(RecorderState())

    val calls = mutableListOf<String>()

    override fun start(): Boolean {
        calls += "start"
        if (available) state.value = RecorderState(recording = true)
        return available
    }

    override fun pause() {
        calls += "pause"
        state.value = state.value.copy(paused = true)
    }

    override fun resume() {
        calls += "resume"
        state.value = state.value.copy(paused = false)
    }

    override fun stop(): RecordedMemo? {
        calls += "stop"
        state.value = RecorderState()
        return memo
    }

    override fun cancel() {
        calls += "cancel"
        state.value = RecorderState()
    }
}
