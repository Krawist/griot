package com.griot.feature.library.folder

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongStatus
import com.griot.core.data.organisation.FolderRepository
import java.text.Collator
import java.util.Locale

/**
 * The songs kept by [filters], in their order. [archives] keeps the songs in « Archives » whatever the filters say, as
 * in the « Archives » folder itself.
 */
internal fun List<LibrarySong>.filteredBy(filters: FolderFilters, archives: Boolean = false): List<LibrarySong> =
    filter { song ->
        (archives || filters.includeArchives || FolderRepository.ARCHIVES_ID !in song.folderIds) &&
            (filters.genres.isEmpty() || song.genreIds.any { it in filters.genres }) &&
            (filters.language == null || song.language == filters.language) &&
            song.isIn(filters.folder) &&
            (filters.states.isEmpty() || filters.states.any { song.isIn(it) })
    }.sortedWith(filters.sort.comparator())

/** How many filters differ from none, the order included: the count on the filter button. */
internal fun FolderFilters.activeCount(): Int = listOf(
    sort != SongSort.MODIFIED,
    genres.isNotEmpty(),
    language != null,
    folder != null,
    states.isNotEmpty(),
    includeArchives,
).count { it }

private fun LibrarySong.isIn(folder: String?): Boolean = when (folder) {
    null -> true
    FolderFilters.NO_FOLDER -> folderIds.isEmpty()
    else -> folder in folderIds
}

private fun LibrarySong.isIn(state: SongState): Boolean = when (state) {
    SongState.DRAFT -> status == SongStatus.DRAFT
    SongState.IN_PROGRESS -> status == SongStatus.IN_PROGRESS
    SongState.UNTITLED -> title.isBlank()
}

// Titles compare as the French read them: accents and case aside; the songs without a title come last.
private val Titles = Collator.getInstance(Locale.FRENCH).apply { strength = Collator.PRIMARY }

private fun SongSort.comparator(): Comparator<LibrarySong> = when (this) {
    SongSort.MODIFIED -> compareByDescending { it.updatedAt }
    SongSort.CREATED -> compareByDescending { it.createdAt }
    SongSort.TITLE -> compareBy<LibrarySong> {
        it.title.isBlank()
    }.thenComparator { a, b -> Titles.compare(a.title, b.title) }
}
