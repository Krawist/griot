package com.griot.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.ai.AiAssistant
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.tidy.TidyUpRepository
import com.griot.feature.library.tidy.songCount
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

/** The home screen: the songs of the last 24 hours (or the last one touched), the folders, and the search. */
@Suppress("LongParameterList") // The songs, their folders and genres, the tidy-up and the AI.
@HiltViewModel
internal class LibraryViewModel @Inject constructor(
    private val songs: SongRepository,
    private val folders: FolderRepository,
    genres: GenreRepository,
    tidy: TidyUpRepository,
    private val assistant: AiAssistant,
    preferences: UserPreferencesRepository,
    private val items: LibraryItems,
    exporter: MarkdownExporter,
    private val clock: Clock,
) : ViewModel() {
    private val query = MutableStateFlow("")

    /** Rename, duplicate or delete a song long pressed. */
    val actions = SongActions(songs = songs, folders = folders, exporter = exporter, scope = viewModelScope)

    private val library = combine(songs.observeLibrary(), folders.observeFolders(), genres.observeGenres()) {
            library,
            folders,
            genres,
        ->
        Triple(library, folders, genres)
    }

    // Whether a model can answer: the tidy-up then counts the songs the AI would name or file.
    private val aiAvailable = MutableStateFlow(false)

    private val tidyUpCount = combine(tidy.observeDue(), tidy.observeCandidates(), aiAvailable) { due, candidates, ai ->
        if (due) candidates.songCount(withAi = ai) else 0
    }

    val uiState: StateFlow<LibraryUiState> = combine(
        library,
        preferences.authorName,
        query,
        actions.deleted,
        tidyUpCount,
    ) { (library, folders, genres), authorName, query, deleted, tidyUpCount ->
        val now = Instant.now(clock)
        val all = library.map { items.itemOf(song = it, folders = folders, genres = genres, now = now) }
        val recent = all.filter { it.song.isRecent(now) }
        LibraryUiState(
            authorName = authorName?.takeIf { it.isNotBlank() },
            query = query,
            results = if (query.isBlank()) emptyList() else all.filter { it.song.matches(query) },
            today = recent.take(TODAY_SONGS),
            recentCount = recent.size,
            favoriteCount = library.count { it.favorite },
            resume = all.firstOrNull()?.takeIf { recent.isEmpty() }?.let { item ->
                ResumeSong(item = item, verses = item.song.firstSection.filter { it.isNotBlank() }.take(RESUME_VERSES))
            },
            folders = folders,
            songCount = library.size,
            tidyUpCount = tidyUpCount,
            loading = false,
            deleted = deleted,
        )
    }
        // The rhymes are read from the dictionaries: never on the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), LibraryUiState())

    init {
        viewModelScope.launch { aiAvailable.value = assistant.availability().location != null }
    }

    fun onQueryChange(text: String) {
        query.value = text
    }

    /** Creates an empty song, then opens it with [onCreated]. */
    fun onNewSong(onCreated: (String) -> Unit) {
        viewModelScope.launch { onCreated(songs.createSong()) }
    }

    /** Creates a folder named [name]. */
    fun onNewFolder(name: String) {
        if (name.isNotBlank()) viewModelScope.launch { folders.createFolder(name) }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val RESUME_VERSES = 2

        // The home screen shows the last three: « Récents » holds the others.
        const val TODAY_SONGS = 3
    }
}
