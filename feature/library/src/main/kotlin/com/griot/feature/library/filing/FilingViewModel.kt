package com.griot.feature.library.filing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.song.SongRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Files the songs [songIds] in folders and tags them with a genre, in one go: nothing changes before « Valider ». A
 * folder holding some of the songs only, or a genre they do not share, stays as it is unless the author taps it.
 */
@HiltViewModel(assistedFactory = FilingViewModel.Factory::class)
internal class FilingViewModel @AssistedInject constructor(
    @Assisted private val songIds: List<String>,
    private val songs: SongRepository,
    private val folders: FolderRepository,
    private val genres: GenreRepository,
) : ViewModel() {
    // The folders tapped, checked or not; the others keep the songs' state.
    private val folderChoices = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // The genre tapped: `null` until one is, then the genre's id, or « none » for no genre.
    private val genreChoice = MutableStateFlow<GenreChoice?>(null)

    val uiState: StateFlow<FilingUiState> = combine(
        songs.observeLibrary(),
        folders.observeFolders(),
        genres.observeGenres(),
        folderChoices,
        genreChoice,
    ) { library, folders, genres, folderChoices, genreChoice ->
        val filed = library.filter { it.id in songIds }
        val sharedGenre = filed.map { it.genreId }.distinct().singleOrNull()
        FilingUiState(
            title = filed.singleOrNull()?.title,
            songCount = filed.size,
            folders = folders.map { folder ->
                val holding = filed.count { folder.id in it.folderIds }
                val check = when (folderChoices[folder.id]) {
                    true -> FolderCheck.CHECKED
                    false -> FolderCheck.UNCHECKED
                    null -> when (holding) {
                        0 -> FolderCheck.UNCHECKED
                        filed.size -> FolderCheck.CHECKED
                        else -> FolderCheck.MIXED
                    }
                }
                FilingFolder(folder = folder, check = check)
            },
            genres = genres,
            genre = if (genreChoice != null) genreChoice.genreId else sharedGenre,
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), FilingUiState())

    /** Checks the folder [folderId], or unchecks it when the songs are all in it. */
    fun onFolderTapped(folderId: String) {
        val check = uiState.value.folders.firstOrNull { it.folder.id == folderId }?.check ?: return
        folderChoices.update { it + (folderId to (check != FolderCheck.CHECKED)) }
    }

    /** « Aucun dossier »: unchecks every folder. */
    fun onNoFolder() {
        folderChoices.value = uiState.value.folders.associate { it.folder.id to false }
    }

    /** Chooses the genre [genreId]; a tap on the genre chosen clears it. */
    fun onGenreTapped(genreId: String) {
        val chosen = uiState.value.genre == genreId
        genreChoice.value = GenreChoice(genreId = genreId.takeUnless { chosen })
    }

    /** Creates a folder named [name] and checks it. */
    fun onNewFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = folders.createFolder(name)
            folderChoices.update { it + (id to true) }
        }
    }

    /** Creates a genre named [name] and chooses it. */
    fun onNewGenre(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { genreChoice.value = GenreChoice(genreId = genres.createGenre(name)) }
    }

    /** Applies the folders and the genre tapped to the songs, then [onDone]. */
    fun onValidate(onDone: () -> Unit) {
        val state = uiState.value
        val add = state.folders.filter { folderChoices.value[it.folder.id] == true }.map { it.folder.id }.toSet()
        val remove = state.folders.filter { folderChoices.value[it.folder.id] == false }.map { it.folder.id }.toSet()
        val genre = genreChoice.value
        viewModelScope.launch {
            if (add.isNotEmpty() ||
                remove.isNotEmpty()
            ) {
                folders.fileSongs(songIds = songIds, add = add, remove = remove)
            }
            if (genre != null) songs.setGenre(ids = songIds, genreId = genre.genreId)
            onDone()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(songIds: List<String>): FilingViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
