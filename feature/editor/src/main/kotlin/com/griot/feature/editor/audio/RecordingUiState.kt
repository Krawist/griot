package com.griot.feature.editor.audio

/**
 * What the recording sheet shows.
 *
 * @property targets What the memo can be attached to: the verse (when there is one), the sections, the whole song.
 * @property target What it is attached to.
 * @property elapsedMillis How long has been recorded, pauses left out.
 * @property paused Whether the recording is paused.
 * @property levels The last loudness heard, oldest first, from 0 to 1: the waveform.
 * @property failed Whether the microphone could not be used.
 */
internal data class RecordingUiState(
    val targets: List<MemoTarget> = emptyList(),
    val target: MemoTarget = MemoTarget(),
    val elapsedMillis: Long = 0,
    val paused: Boolean = false,
    val levels: List<Float> = emptyList(),
    val failed: Boolean = false,
)
