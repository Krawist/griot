package com.griot.core.data.database.dao

import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind

/**
 * A folder with the number of its songs, read in one query.
 *
 * @property id Id of the folder.
 * @property name Its name; empty for « Archives ».
 * @property color Its colour.
 * @property kind A folder of the author, or « Archives ».
 * @property songCount How many songs out of the trash it holds.
 */
internal data class FolderRow(
    val id: String,
    val name: String,
    val color: FolderColor,
    val kind: FolderKind,
    val songCount: Int,
)
