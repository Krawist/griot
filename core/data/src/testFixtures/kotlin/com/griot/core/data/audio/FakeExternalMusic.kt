package com.griot.core.data.audio

import kotlinx.coroutines.flow.MutableStateFlow

/** [ExternalMusic] for tests: [state] is what plays, [access] the notification access; [calls] records what was asked. */
class FakeExternalMusic(state: ExternalMusicState = ExternalMusicState(), var access: Boolean = false) : ExternalMusic {
    val state = MutableStateFlow(state)

    val calls = mutableListOf<String>()

    override fun observe() = state

    override fun hasAccess() = access

    override fun playPause() {
        calls += "play pause"
    }

    override fun rewind(millis: Long) {
        calls += "rewind $millis"
    }
}
