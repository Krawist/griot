package com.griot.feature.editor.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.audio.AttachmentRepository
import com.griot.core.data.audio.AudioFiles
import com.griot.core.data.audio.AudioPlayer
import com.griot.core.data.audio.CoverReader
import com.griot.core.data.model.AttachmentKind
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

/**
 * The beat of the song [songId]: its newest file on this phone that can be read. The player under the lyrics plays it,
 * and the recording sheet can play it while the author records.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = BeatViewModel.Factory::class)
internal class BeatViewModel @AssistedInject constructor(
    @Assisted songId: String,
    attachments: AttachmentRepository,
    private val files: AudioFiles,
    private val covers: CoverReader,
    private val player: AudioPlayer,
) : ViewModel() {
    private val beat = attachments.observeAttachments(songId)
        .map { list ->
            list.firstOrNull { it.kind == AttachmentKind.FILE && it.onThisPhone && files.isAvailable(it.uri) }
        }
        .distinctUntilChanged()

    private val cover = beat.mapLatest { attachment -> attachment?.let { covers.read(it.uri) } }.onStart { emit(null) }

    /** The beat, or `null` when the song has none on this phone. */
    val uiState: StateFlow<BeatUiState?> = combine(beat, cover, player.state) { attachment, art, playback ->
        attachment?.let {
            val loaded = playback.uri == it.uri
            BeatUiState(
                title = it.title,
                uri = it.uri,
                cover = art,
                playing = loaded && playback.playing,
                positionMillis = if (loaded) playback.positionMillis else 0,
                durationMillis = if (loaded &&
                    playback.durationMillis > 0
                ) {
                    playback.durationMillis
                } else {
                    it.durationMillis ?: 0
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    /** Plays the beat, or pauses it. */
    fun onPlayToggle() {
        val state = uiState.value ?: return
        if (state.playing) player.pause() else player.play(uri = state.uri, title = state.title)
    }

    /** Plays the beat when it does not play yet: the recording sheet's « Jouer le beat ». */
    fun onPlay() {
        val state = uiState.value ?: return
        if (!state.playing) player.play(uri = state.uri, title = state.title)
    }

    fun onPause() {
        player.pause()
    }

    /** « Reculer de 15 secondes », never before the start. */
    fun onRewind() {
        player.rewind(REWIND_MILLIS)
    }

    @AssistedFactory
    interface Factory {
        fun create(songId: String): BeatViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val REWIND_MILLIS = 15_000L
    }
}
