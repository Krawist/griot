package com.griot.feature.library.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.model.FolderColor
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.song.SongRepository
import com.griot.feature.library.FAVORITES_FOLDER_ID
import com.griot.feature.library.LibraryItems
import com.griot.feature.library.RECENT_FOLDER_ID
import com.griot.feature.library.SongActions
import com.griot.feature.library.isRecent
import com.griot.feature.library.isVirtualFolder
import com.griot.feature.library.matches
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
@Suppress("LongParameterList", "TooManyFunctions") // The folder, its songs and filters, their cards, the export.
@HiltViewModel(assistedFactory = FolderViewModel.Factory::class)
internal class FolderViewModel @AssistedInject constructor(
    @Assisted private val folderId: String?,
    private val songs: SongRepository,
    private val folders: FolderRepository,
    genres: GenreRepository,
    private val items: LibraryItems,
    exporter: MarkdownExporter,
    private val clock: Clock,
) : ViewModel() {
    private val filters = MutableStateFlow(FolderFilters())

    // The search typed over the songs.
    private val query = MutableStateFlow("")

    // The filters being chosen in « Filtres et tri »; `null` while it is closed.
    private val draft = MutableStateFlow<FolderFilters?>(null)
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

    // The filters applied and the search, which together choose the songs shown.
    private val search = combine(filters, query) { filters, query -> filters to query }

    val uiState: StateFlow<FolderUiState> = combine(library, search, draft, selection, actions.deleted) {
            (library, folders, genres),
            (filters, query),
            draft,
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
        val found = if (query.isBlank()) inFolder else inFolder.filter { it.matches(query) }
        val present = inFolder.flatMap { it.genreIds }.toSet()
        val archives = folderId == FolderRepository.ARCHIVES_ID
        FolderUiState(
            folder = folder,
            songCount = inFolder.size,
            genres = genres.filter { it.id in present },
            filters = filters,
            filterCount = filters.activeCount(),
            filterSheet = draft?.let {
                FilterSheetState(
                    draft = it,
                    count = found.filteredBy(it, archives = archives).size,
                    genres = genres,
                    folders = if (folder ==
                        null
                    ) {
                        folders.filter { f -> f.id != FolderRepository.ARCHIVES_ID }
                    } else {
                        emptyList()
                    },
                    archivesCount = inFolder.count { song -> FolderRepository.ARCHIVES_ID in song.folderIds },
                    showArchives = !archives,
                )
            },
            query = query,
            items = found.filteredBy(filters, archives = archives)
                .map { items.itemOf(song = it, folders = folders, genres = genres, now = now) },
            moreInArchives = !archives &&
                !filters.includeArchives &&
                found.filteredBy(filters.copy(includeArchives = true)).size >
                found.filteredBy(filters).size,
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

    /** Keeps every genre when [genreId] is `null` (« Tous »); otherwise keeps the genre too, or no longer. */
    fun onGenreSelected(genreId: String?) {
        filters.update { current ->
            when (genreId) {
                null -> current.copy(genres = emptySet())
                in current.genres -> current.copy(genres = current.genres - genreId)
                else -> current.copy(genres = current.genres + genreId)
            }
        }
    }

    /** Keeps the songs whose title or verses hold [text]. */
    fun onQueryChange(text: String) {
        query.value = text
    }

    /** « Effacer les filtres »: every song of the folder, the last changed first, the search cleared. */
    fun onFiltersCleared() {
        filters.value = FolderFilters()
        query.value = ""
    }

    /** « Chercher dans les archives »: the filters applied, the archived songs included. */
    fun onArchivesIncluded() {
        filters.update { it.copy(includeArchives = true) }
    }

    /** « Nouvelle chanson ici »: a new song, filed in this folder, then [onCreated] with its id. */
    fun onNewSongHere(onCreated: (String) -> Unit) {
        val id = folderId?.takeUnless(::isVirtualFolder) ?: return
        viewModelScope.launch {
            val songId = songs.createSong()
            folders.fileSongs(songIds = listOf(songId), add = setOf(id), remove = emptySet())
            onCreated(songId)
        }
    }

    /** Opens « Filtres et tri » on the filters applied. */
    fun onFiltersOpened() {
        draft.value = filters.value
    }

    /** The author changed the filters being chosen to [filters]. */
    fun onFiltersChanged(filters: FolderFilters) {
        if (draft.value != null) draft.value = filters
    }

    /** « Réinitialiser »: no filter, the last changed first. */
    fun onFiltersReset() {
        if (draft.value != null) draft.value = FolderFilters()
    }

    /** « Voir N textes »: applies the filters chosen and closes the sheet. */
    fun onFiltersApplied() {
        draft.value?.let { filters.value = it }
        draft.value = null
    }

    /** The sheet closed without applying: the filters stay as they were. */
    fun onFiltersClosed() {
        draft.value = null
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
