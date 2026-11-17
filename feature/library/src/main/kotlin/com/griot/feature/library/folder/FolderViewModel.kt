package com.griot.feature.library.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.song.SongRepository
import com.griot.feature.library.FAVORITES_FOLDER_ID
import com.griot.feature.library.LibraryItems
import com.griot.feature.library.RECENT_FOLDER_ID
import com.griot.feature.library.SongActions
import com.griot.feature.library.isRecent
import com.griot.feature.library.isVirtualFolder
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The songs of the folder [folderId], of « Récents » ([RECENT_FOLDER_ID]: the songs changed in the last 24 hours), of
 * « Favoris » ([FAVORITES_FOLDER_ID]), or of the whole library when it is `null`, with their filters.
 */
@Suppress("LongParameterList") // The folder, its songs, folders and genres, their cards, and the export.
@HiltViewModel(assistedFactory = FolderViewModel.Factory::class)
internal class FolderViewModel @AssistedInject constructor(
    @Assisted private val folderId: String?,
    songs: SongRepository,
    private val folders: FolderRepository,
    genres: GenreRepository,
    private val items: LibraryItems,
    exporter: MarkdownExporter,
    private val clock: Clock,
) : ViewModel() {
    private val filters = MutableStateFlow(FolderFilters())
    private val selection = MutableStateFlow<Set<String>?>(null)

    /** Rename, duplicate or delete a song long pressed. */
    val actions = SongActions(songs = songs, folders = folders, exporter = exporter, scope = viewModelScope)

    private val library = combine(songs.observeLibrary(), folders.observeFolders(), genres.observeGenres()) {
            library,
            folders,
            genres,
        ->
        Triple(library, folders, genres)
    }

    val uiState: StateFlow<FolderUiState> = combine(library, filters, selection, actions.deleted) {
            (library, folders, genres),
            filters,
            selection,
            deleted,
        ->
        val now = Instant.now(clock)
        val folder = folders.firstOrNull { it.id == folderId }
        val inFolder = library.filter { song ->
            when (folderId) {
                null -> true
                RECENT_FOLDER_ID -> song.isRecent(now)
                FAVORITES_FOLDER_ID -> song.favorite
                else -> folderId in song.folderIds
            }
        }
        val present = inFolder.flatMap { it.genreIds }.toSet()
        FolderUiState(
            folder = folder,
            songCount = inFolder.size,
            genres = genres.filter { it.id in present },
            genre = filters.genre,
            language = filters.language,
            items = inFolder
                .filter { filters.genre == null || filters.genre in it.genreIds }
                .filter { filters.language == null || it.language == filters.language }
                .map { items.itemOf(song = it, folders = folders, genres = genres, now = now) },
            selecting = selection != null,
            selected = selection.orEmpty(),
            loading = false,
            virtual = folderId?.takeIf(::isVirtualFolder),
            missing = folderId != null && !isVirtualFolder(folderId) && folder == null,
            deleted = deleted,
        )
    }
        // The rhymes are read from the dictionaries: never on the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), FolderUiState())

    /** Keeps the songs of the genre [genreId], or of every genre when `null` (« Tous »). */
    fun onGenreSelected(genreId: String?) {
        filters.update { it.copy(genre = genreId) }
    }

    /** Keeps the songs in [language], or in both when `null` (« Toutes »). */
    fun onLanguageSelected(language: SongLanguage?) {
        filters.update { it.copy(language = language) }
    }

    /** Turns the multiple selection on, with nothing selected, or off. */
    fun onSelecting(selecting: Boolean) {
        selection.value = if (selecting) emptySet() else null
    }

    /** Selects the song [songId], or leaves it when it was selected. */
    fun onToggleSelected(songId: String) {
        selection.update { selected -> selected?.let { if (songId in it) it - songId else it + songId } }
    }

    fun onRenameFolder(name: String) {
        val id = folderId?.takeUnless(::isVirtualFolder) ?: return
        if (name.isNotBlank()) viewModelScope.launch { folders.renameFolder(id = id, name = name) }
    }

    fun onFolderColor(color: FolderColor) {
        val id = folderId?.takeUnless(::isVirtualFolder) ?: return
        viewModelScope.launch { folders.setFolderColor(id = id, color = color) }
    }

    /** Deletes the folder; its songs go back to « Aucun dossier ». */
    fun onDeleteFolder() {
        val id = folderId?.takeUnless(::isVirtualFolder) ?: return
        viewModelScope.launch { folders.deleteFolder(id) }
    }

    @AssistedFactory
    interface Factory {
        fun create(folderId: String?): FolderViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
