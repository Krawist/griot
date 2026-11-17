package com.griot.feature.editor.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.audio.AttachmentRepository
import com.griot.core.data.audio.VoiceRecorder
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.sectionLabels
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Records a voice memo for the song [songId] as soon as it opens. With a [verseId], the memo can be attached to that
 * verse, and is by default when [onVerse]; otherwise to the verse's section, or to the whole song.
 */
@HiltViewModel(assistedFactory = RecordingViewModel.Factory::class)
internal class RecordingViewModel @AssistedInject constructor(
    @Assisted("songId") private val songId: String,
    @Assisted("verseId") private val verseId: String?,
    @Assisted private val onVerse: Boolean,
    private val editor: SongEditorRepository,
    private val recorder: VoiceRecorder,
    private val attachments: AttachmentRepository,
) : ViewModel() {
    private val state = MutableStateFlow(RecordingUiState())

    val uiState: StateFlow<RecordingUiState> = state.asStateFlow()

    init {
        viewModelScope.launch { state.update { it.copy(failed = !recorder.start()) } }
        viewModelScope.launch { loadTargets() }
        viewModelScope.launch {
            recorder.state.collect { recording ->
                state.update { current ->
                    val levels = if (recording.recording && !recording.paused) {
                        (current.levels + recording.level).takeLast(BARS)
                    } else {
                        current.levels
                    }
                    current.copy(elapsedMillis = recording.elapsedMillis, paused = recording.paused, levels = levels)
                }
            }
        }
    }

    private suspend fun loadTargets() {
        val document = editor.observeDocument(songId).first() ?: return
        val labels = sectionLabels(document.sections)
        val sections = document.sections.mapIndexedNotNull { index, section ->
            MemoTarget(sectionId = section.id, label = labels[index]).takeIf { section.repeatOf == null }
        }
        val verse = verseId?.let { id ->
            val index = document.sections.indexOfFirst { section -> section.verses.any { it.id == id } }
            document.sections.getOrNull(index)?.let { section ->
                MemoTarget(
                    sectionId = section.id,
                    verseId = id,
                    label = labels[index],
                    verseNumber = section.verses.indexOfFirst { it.id == id } + 1,
                )
            }
        }
        val song = MemoTarget()
        val target = when {
            verse != null && onVerse -> verse
            verse != null -> sections.firstOrNull { it.sectionId == verse.sectionId } ?: song
            else -> song
        }
        state.update { it.copy(targets = listOfNotNull(verse) + sections + song, target = target) }
    }

    fun onTarget(target: MemoTarget) {
        state.update { it.copy(target = target) }
    }

    /** Pauses, or resumes when paused. */
    fun onPauseToggle() {
        if (state.value.paused) recorder.resume() else recorder.pause()
    }

    /** Keeps the memo, named [title], attached to the target chosen, then [onDone]. */
    fun onStop(title: String, onDone: () -> Unit) {
        val memo = recorder.stop()
        val target = state.value.target
        viewModelScope.launch {
            if (memo != null) {
                attachments.addMemo(
                    songId = songId,
                    memo = memo,
                    title = title,
                    sectionId = target.sectionId,
                    verseId = target.verseId,
                )
            }
            onDone()
        }
    }

    /** Throws the memo away. */
    fun onCancel() {
        recorder.cancel()
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("songId") songId: String,
            @Assisted("verseId") verseId: String?,
            onVerse: Boolean,
        ): RecordingViewModel
    }

    private companion object {
        // The waveform shows this many bars.
        const val BARS = 44
    }
}
