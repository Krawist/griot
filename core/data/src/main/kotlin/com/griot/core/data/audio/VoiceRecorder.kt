package com.griot.core.data.audio

import kotlinx.coroutines.flow.StateFlow

/** Records voice memos from the microphone, in the app's files: one at a time. */
interface VoiceRecorder {
    /** Where the recording is, updated about ten times a second while it runs. */
    val state: StateFlow<RecorderState>

    /** Starts a memo; `false` when the microphone cannot be used (no permission, used elsewhere). */
    fun start(): Boolean

    fun pause()

    fun resume()

    /** Ends the memo and keeps it; `null` when nothing was recorded. */
    fun stop(): RecordedMemo?

    /** Ends the memo and throws it away. */
    fun cancel()
}
