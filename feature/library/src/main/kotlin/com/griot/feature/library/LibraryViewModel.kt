package com.griot.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.design.format.relativeTime
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.rhymeLetters
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
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

@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val songs: SongRepository,
    preferences: UserPreferencesRepository,
    private val engines: PhoneticEngines,
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
                .map { song ->
                    LibraryItem(
                        song = song,
                        updated = relativeTime(song.updatedAt, now = now, zone = clock.zone),
                        rhymes = rhymesOf(song),
                    )
                },
            hasSongs = library.isNotEmpty(),
            loading = false,
            deleted = deleted,
        )
    }
        // The rhymes are read from the dictionaries: never on the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    // The rhyme ranks of the first section, read in English for an English song and in French otherwise: each engine
    // reads the other language's words.
    private fun rhymesOf(song: LibrarySong): List<Int> {
        val language = if (song.language == SongLanguage.ENGLISH) PhoneticLanguage.ENGLISH else PhoneticLanguage.FRENCH
        val engine = engines.of(language)
        return rhymeLetters(song.firstSection.map { engine.rhyme(it) }).filterNotNull()
    }

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
