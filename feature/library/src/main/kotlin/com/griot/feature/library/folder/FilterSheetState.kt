package com.griot.feature.library.folder

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre

/**
 * What « Filtres et tri » shows while it is open: the filters being chosen, not applied yet.
 *
 * @property draft The filters chosen so far.
 * @property count How many songs [draft] keeps: « Voir N textes ».
 * @property genres Every genre, in order.
 * @property folders The folders to keep, but « Archives »; empty where the songs of one folder show.
 * @property archivesCount How many songs of the screen are in « Archives ».
 * @property showArchives Whether « Inclure les archives » shows: not in the « Archives » folder, which shows them all.
 */
internal data class FilterSheetState(
    val draft: FolderFilters,
    val count: Int,
    val genres: List<Genre>,
    val folders: List<Folder>,
    val archivesCount: Int,
    val showArchives: Boolean,
)
