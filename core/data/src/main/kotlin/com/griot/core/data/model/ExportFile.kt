package com.griot.core.data.model

/**
 * A file made by an export, ready to share or save.
 *
 * @property name Its name, shown to the user and kept when saved.
 * @property mimeType Its type: `text/markdown` for a song, `application/zip` for all of them.
 * @property path Its absolute path in the app's cache.
 */
data class ExportFile(val name: String, val mimeType: String, val path: String)
