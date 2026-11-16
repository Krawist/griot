package com.griot.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.preferences.UserPreferencesRepository
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
internal class LibraryViewModel @Inject constructor(
    private val songs: SongRepository,
    preferences: UserPreferencesRepository,
    private val clock: Clock,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(LibraryFilter.ALL)
    private val deleted = MutableStateFlow<DeletedSong?>(null)

    val uiState: StateFlow<LibraryUiState> = combine(
        songs.observeLibrary(),
        preferences.authorName,
        query,
        filter,
        deleted,
    ) { library, authorName, query, filter, deleted ->
        val now = Instant.now(clock)
        LibraryUiState(
            authorName = authorName?.takeIf { it.isNotBlank() },
            query = query,
            filter = filter,
            items = library
                .filter { it.isIn(filter) && it.matches(query) }
                .map { LibraryItem(song = it, updated = relativeTime(it.updatedAt, now = now, zone = clock.zone)) },
            hasSongs = library.isNotEmpty(),
            loading = false,
            deleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    fun onQueryChange(text: String) {
        query.value = text
    }

    fun onFilterSelected(selected: LibraryFilter) {
        filter.value = selected
    }

    /** Creates an empty song, then opens it with [onCreated]. */
    fun onNewSong(onCreated: (String) -> Unit) {
        viewModelScope.launch { onCreated(songs.createSong()) }
    }

    fun onRename(id: String, title: String) {
        viewModelScope.launch { songs.renameSong(id = id, title = title.trim()) }
    }

    /** Copies the song under [title], such as « Les néons (copie) ». */
    fun onDuplicate(id: String, title: String) {
        viewModelScope.launch { songs.duplicateSong(id = id, title = title) }
    }

    /** Moves the song to the trash and shows the undo bar; [title] is the one the bar shows. */
    fun onDelete(id: String, title: String) {
        viewModelScope.launch {
            songs.deleteSong(id)
            deleted.value = DeletedSong(id = id, title = title)
        }
    }

    /** Takes the song just deleted out of the trash. */
    fun onUndoDelete() {
        val song = deleted.value ?: return
        deleted.value = null
        viewModelScope.launch { songs.restoreSong(song.id) }
    }

    fun onUndoDismissed() {
        deleted.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
