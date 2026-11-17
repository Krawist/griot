package com.griot.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The home screen: the songs of the last 24 hours (or the last one touched), the folders, and the search. */
@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val songs: SongRepository,
    private val folders: FolderRepository,
    genres: GenreRepository,
    preferences: UserPreferencesRepository,
    private val items: LibraryItems,
    private val clock: Clock,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val deleted = MutableStateFlow<DeletedSong?>(null)

    private val library = combine(songs.observeLibrary(), folders.observeFolders(), genres.observeGenres()) {
            library,
            folders,
            genres,
        ->
        Triple(library, folders, genres)
    }

    val uiState: StateFlow<LibraryUiState> = combine(library, preferences.authorName, query, deleted) {
            (library, folders, genres),
            authorName,
            query,
            deleted,
        ->
        val now = Instant.now(clock)
        val all = library.map { items.itemOf(song = it, folders = folders, genres = genres, now = now) }
        // A rolling window: a song changed 25 hours ago is no longer of today.
        val today = all.filter { Duration.between(it.song.updatedAt, now) < TODAY }
        LibraryUiState(
            authorName = authorName?.takeIf { it.isNotBlank() },
            query = query,
            results = if (query.isBlank()) emptyList() else all.filter { it.song.matches(query) },
            today = today,
            resume = all.firstOrNull()?.takeIf { today.isEmpty() }?.let { item ->
                ResumeSong(item = item, verses = item.song.firstSection.filter { it.isNotBlank() }.take(RESUME_VERSES))
            },
            folders = folders,
            songCount = library.size,
            loading = false,
            deleted = deleted,
        )
    }
        // The rhymes are read from the dictionaries: never on the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    fun onQueryChange(text: String) {
        query.value = text
    }

    /** Creates an empty song, then opens it with [onCreated]. */
    fun onNewSong(onCreated: (String) -> Unit) {
        viewModelScope.launch { onCreated(songs.createSong()) }
    }

    /** Creates a folder named [name], in the next of the accent colours. */
    fun onNewFolder(name: String) {
        if (name.isBlank()) return
        val count = uiState.value.folders.count { it.kind == FolderKind.CUSTOM }
        viewModelScope.launch { folders.createFolder(name = name, color = FolderColors[count % FolderColors.size]) }
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
        const val RESUME_VERSES = 2
        val TODAY: Duration = Duration.ofHours(24)

        // New folders take the accents in turn.
        val FolderColors = listOf(FolderColor.MAGENTA, FolderColor.TURQUOISE, FolderColor.ORANGE, FolderColor.YELLOW)
    }
}
