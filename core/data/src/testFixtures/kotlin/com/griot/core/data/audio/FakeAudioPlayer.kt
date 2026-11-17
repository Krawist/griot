package com.griot.core.data.audio

import kotlinx.coroutines.flow.MutableStateFlow

/** [AudioPlayer] for tests: it plays what is asked at once; [calls] records what was asked, such as `"play my-uri"`. */
class FakeAudioPlayer : AudioPlayer {
    override val state = MutableStateFlow(PlayerState())

    val calls = mutableListOf<String>()

    override fun play(uri: String, title: String): Boolean {
        calls += "play $uri $title"
        state.value = state.value.copy(uri = uri, playing = true)
        return true
    }

    override fun pause() {
        calls += "pause"
        state.value = state.value.copy(playing = false)
    }

    override fun rewind(millis: Long) {
        calls += "rewind $millis"
    }

    override fun stop() {
        calls += "stop"
        state.value = PlayerState()
    }
}
