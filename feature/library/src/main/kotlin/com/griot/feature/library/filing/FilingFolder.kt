package com.griot.feature.library.filing

import com.griot.core.data.model.Folder

/**
 * A folder of the sheet, with whether the songs are in it.
 *
 * @property folder The folder.
 * @property check Whether the songs are in it, with the author's taps.
 */
internal data class FilingFolder(val folder: Folder, val check: FilingCheck)
