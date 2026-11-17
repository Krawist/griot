package com.griot.feature.library

import com.griot.core.data.model.ExportFile

/**
 * A song exported from its action sheet, waiting to leave the app.
 *
 * @property file The Markdown file of the song.
 * @property share Whether it goes straight to the system's share sheet (« Partager »); otherwise the export sheet
 *   offers to share or save it (« Exporter en Markdown »).
 */
internal data class SongExport(val file: ExportFile, val share: Boolean)
