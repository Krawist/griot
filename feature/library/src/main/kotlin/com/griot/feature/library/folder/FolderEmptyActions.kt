package com.griot.feature.library.folder

/**
 * What the buttons of a [FolderEmptyState] do.
 *
 * @property onClearFilters « Effacer les filtres ».
 * @property onSearchArchives « Chercher dans les archives »: the archived songs included.
 * @property onNewSongHere « Nouvelle chanson ici »: a new song, filed in the folder, opened.
 * @property onOpenAll « Ranger des textes ici » and « Tous les textes »: every text.
 */
internal data class FolderEmptyActions(
    val onClearFilters: () -> Unit,
    val onSearchArchives: () -> Unit,
    val onNewSongHere: () -> Unit,
    val onOpenAll: () -> Unit,
)
