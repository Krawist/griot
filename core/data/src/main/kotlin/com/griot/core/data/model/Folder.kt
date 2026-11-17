package com.griot.core.data.model

/**
 * A folder of songs, one level only.
 *
 * @property id Stable id, the same on every device; « archives » for the built-in folder.
 * @property name The name the author gave; empty for « Archives », whose name comes from the interface's language.
 * @property color Its colour.
 * @property kind A folder of the author, or « Archives ».
 * @property songCount How many songs out of the trash it holds.
 */
data class Folder(val id: String, val name: String, val color: FolderColor, val kind: FolderKind, val songCount: Int)
