package com.griot.feature.library.folder

import com.griot.core.data.model.SongLanguage

/**
 * The filters and the order of the folder screen (« Filtres et tri »).
 *
 * @property sort The order of the songs.
 * @property genres The ids of the genres kept, a song having one of them; empty for every genre.
 * @property language The language kept; `null` for both.
 * @property folder The folder kept, [NO_FOLDER] for the songs in none; `null` for every folder. Only where songs of
 *   several folders mix.
 * @property states The states kept, a song being in one of them; empty for every state.
 * @property includeArchives Whether the songs in « Archives » show; the « Archives » folder always shows its own.
 */
internal data class FolderFilters(
    val sort: SongSort = SongSort.MODIFIED,
    val genres: Set<String> = emptySet(),
    val language: SongLanguage? = null,
    val folder: String? = null,
    val states: Set<SongState> = emptySet(),
    val includeArchives: Boolean = false,
) {
    companion object {
        /** [folder] for the songs in no folder (« Sans dossier »). */
        const val NO_FOLDER = "none"
    }
}
