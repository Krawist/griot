package com.griot.feature.library

/**
 * What the library shows.
 *
 * @property authorName Name greeted above the title; `null` when the user gave none.
 * @property query Text typed in the search field.
 * @property filter Filter chip selected.
 * @property items Songs matching the search and the filter, last changed first.
 * @property hasSongs Whether the library holds any song at all, to tell an empty library from an empty search.
 * @property loading Whether the songs are still being read, so that no empty state flashes at start.
 */
internal data class LibraryUiState(
    val authorName: String? = null,
    val query: String = "",
    val filter: LibraryFilter = LibraryFilter.ALL,
    val items: List<LibraryItem> = emptyList(),
    val hasSongs: Boolean = false,
    val loading: Boolean = true,
)
