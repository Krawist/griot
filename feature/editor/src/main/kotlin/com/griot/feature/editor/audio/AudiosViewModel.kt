package com.griot.feature.editor.audio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.audio.AttachmentRepository
import com.griot.core.data.audio.AudioFiles
import com.griot.core.data.audio.AudioPlayer
import com.griot.core.data.model.AttachmentKind
import com.griot.core.data.song.SongEditorRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The audios of the song [songId]: files and links, memos, whether each file can still be read, and listening. */
@HiltViewModel(assistedFactory = AudiosViewModel.Factory::class)
internal class AudiosViewModel @AssistedInject constructor(
    @Assisted private val songId: String,
    editor: SongEditorRepository,
    private val attachments: AttachmentRepository,
    private val files: AudioFiles,
    private val player: AudioPlayer,
) : ViewModel() {
    // The message to show once: a file that could not be read.
    private val failed = MutableStateFlow(false)

    /** Whether the last file picked could not be read, until the message is shown. */
    val fileFailed: StateFlow<Boolean> = failed.asStateFlow()

    val uiState: StateFlow<AudiosUiState> = combine(
        editor.observeDocument(songId).map { it?.title.orEmpty() },
        attachments.observeAttachments(songId).map { list ->
            list.map { attachment ->
                val file = attachment.kind == AttachmentKind.FILE && attachment.onThisPhone
                AudioItem(attachment = attachment, missing = file && !files.isAvailable(attachment.uri))
            }
        },
        player.state,
    ) { title, items, playback ->
        AudiosUiState(
            songTitle = title,
            playing = playback.uri?.takeIf { playback.playing },
            beats = items.filter { it.attachment.kind != AttachmentKind.MEMO },
            memos = items.filter { it.attachment.kind == AttachmentKind.MEMO },
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), AudiosUiState())

    /** Adds the file [uri] picked by the author. */
    fun onFilePicked(uri: String) {
        viewModelScope.launch { if (attachments.addFile(songId = songId, uri = uri) == null) failed.value = true }
    }

    /** Adds the link [url], named [title] or after its address. */
    fun onLink(url: String, title: String) {
        if (url.isNotBlank()) viewModelScope.launch { attachments.addLink(songId = songId, url = url, title = title) }
    }

    /** Points the file [id] to the file [uri] picked again. */
    fun onRelink(id: String, uri: String) {
        viewModelScope.launch { if (!attachments.relink(id = id, uri = uri)) failed.value = true }
    }

    fun onRename(id: String, title: String) {
        viewModelScope.launch { attachments.rename(id = id, title = title) }
    }

    /** Removes the audio [id]; a file stays on the phone. */
    fun onDelete(id: String) {
        viewModelScope.launch { attachments.delete(id) }
    }

    /** Plays the memo or file [item], or pauses it when it plays. */
    fun onPlayToggle(item: AudioItem) {
        val uri = item.attachment.uri
        if (uiState.value.playing == uri) {
            player.pause()
        } else if (!player.play(uri = uri, title = item.attachment.title)) {
            failed.value = true
        }
    }

    fun onFailureShown() {
        failed.value = false
    }

    @AssistedFactory
    interface Factory {
        fun create(songId: String): AudiosViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
