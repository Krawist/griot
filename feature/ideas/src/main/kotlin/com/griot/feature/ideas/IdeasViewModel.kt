package com.griot.feature.ideas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.idea.IdeaRepository
import com.griot.core.data.model.Idea
import com.griot.core.data.song.SongRepository
import com.griot.core.design.format.relativeTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
internal class IdeasViewModel @Inject constructor(
    private val ideas: IdeaRepository,
    songs: SongRepository,
    private val clock: Clock,
) : ViewModel() {
    private val deleted = MutableStateFlow<Idea?>(null)

    val uiState: StateFlow<IdeasUiState> = combine(ideas.observeIdeas(), songs.observeLibrary(), deleted) {
            list,
            library,
            deleted,
        ->
        val now = Instant.now(clock)
        IdeasUiState(
            loading = false,
            items = list.map { IdeaItem(idea = it, saved = relativeTime(it.createdAt, now = now, zone = clock.zone)) },
            songs = library,
            deleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), IdeasUiState())

    /** Saves [text] as a free idea; a blank text saves nothing. */
    fun onAdd(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { ideas.addIdea(text) }
    }

    /** Saves the image at [uri] as a free idea. */
    fun onImagePicked(uri: String) {
        viewModelScope.launch { ideas.addImageIdea(uri) }
    }

    fun onEdit(id: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch { ideas.updateIdea(id = id, text = text) }
    }

    /** Links the idea to the song [songId]; `null` frees it. */
    fun onLink(id: String, songId: String?) {
        viewModelScope.launch { ideas.linkIdea(id = id, songId = songId) }
    }

    /** Starts a new song from the idea, then opens it with [onCreated]. */
    fun onStartSong(id: String, onCreated: (String) -> Unit) {
        viewModelScope.launch { onCreated(ideas.startSong(id)) }
    }

    /** Deletes the idea and shows the undo bar. */
    fun onDelete(idea: Idea) {
        viewModelScope.launch {
            ideas.deleteIdea(idea.id)
            deleted.value = idea
        }
    }

    fun onUndoDelete() {
        val idea = deleted.value ?: return
        deleted.value = null
        viewModelScope.launch { ideas.restoreIdea(idea.id) }
    }

    fun onUndoDismissed() {
        deleted.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
