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
 * Files the songs [songIds] in folders and tags them with genres, in one go: nothing changes before « Valider ». A
 * folder holding some of the songs only, or a genre some of them have, stays as it is unless the author taps it.
 */
@HiltViewModel(assistedFactory = FilingViewModel.Factory::class)
internal class FilingViewModel @AssistedInject constructor(
    @Assisted private val songIds: List<String>,
    songs: SongRepository,
    private val folders: FolderRepository,
    private val genres: GenreRepository,
) : ViewModel() {
    // The folders tapped, checked or not; the others keep the songs' state.
    private val folderChoices = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // The genres tapped, checked or not; the others keep the songs' state.
    private val genreChoices = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val uiState: StateFlow<FilingUiState> = combine(
        songs.observeLibrary(),
        folders.observeFolders(),
        genres.observeGenres(),
        folderChoices,
        genreChoices,
    ) { library, folders, genres, folderChoices, genreChoices ->
        val filed = library.filter { it.id in songIds }
        FilingUiState(
            title = filed.singleOrNull()?.title,
            songCount = filed.size,
            folders = folders.map { folder ->
                val holding = filed.count { folder.id in it.folderIds }
                FilingFolder(folder = folder, check = checkOf(folderChoices[folder.id], holding, filed.size))
            },
            genres = genres.map { genre ->
                val tagged = filed.count { genre.id in it.genreIds }
                FilingGenre(genre = genre, check = checkOf(genreChoices[genre.id], tagged, filed.size))
            },
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), FilingUiState())

    /** Checks the folder [folderId], or unchecks it when the songs are all in it. */
    fun onFolderTapped(folderId: String) {
        val check = uiState.value.folders.firstOrNull { it.folder.id == folderId }?.check ?: return
        folderChoices.update { it + (folderId to (check != FilingCheck.CHECKED)) }
    }

    /** « Aucun dossier »: unchecks every folder. */
    fun onNoFolder() {
        folderChoices.value = uiState.value.folders.associate { it.folder.id to false }
    }

    /** Tags the songs with the genre [genreId], or takes it off when they all have it. */
    fun onGenreTapped(genreId: String) {
        val check = uiState.value.genres.firstOrNull { it.genre.id == genreId }?.check ?: return
        genreChoices.update { it + (genreId to (check != FilingCheck.CHECKED)) }
    }

    /** Creates a folder named [name] and checks it. */
    fun onNewFolder(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = folders.createFolder(name)
            folderChoices.update { it + (id to true) }
        }
    }

    /** Creates a genre named [name] and checks it. */
    fun onNewGenre(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = genres.createGenre(name)
            genreChoices.update { it + (id to true) }
        }
    }

    /** Applies the folders and the genres tapped to the songs, then [onDone]. */
    fun onValidate(onDone: () -> Unit) {
        val folderTaps = folderChoices.value
        val genreTaps = genreChoices.value
        viewModelScope.launch {
            if (folderTaps.isNotEmpty()) {
                folders.fileSongs(songIds = songIds, add = folderTaps.checked(true), remove = folderTaps.checked(false))
            }
            if (genreTaps.isNotEmpty()) {
                genres.tagSongs(songIds = songIds, add = genreTaps.checked(true), remove = genreTaps.checked(false))
            }
            onDone()
        }
    }

    // Tapped: as tapped; else all the songs, none, or some only, by how many of the [total] are [holding].
    private fun checkOf(tapped: Boolean?, holding: Int, total: Int) = when (tapped) {
        true -> FilingCheck.CHECKED
        false -> FilingCheck.UNCHECKED
        null -> when (holding) {
            0 -> FilingCheck.UNCHECKED
            total -> FilingCheck.CHECKED
            else -> FilingCheck.MIXED
        }
    }

    // The ids tapped to [checked].
    private fun Map<String, Boolean>.checked(checked: Boolean) = filterValues { it == checked }.keys

    @AssistedFactory
    interface Factory {
        fun create(songIds: List<String>): FilingViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
