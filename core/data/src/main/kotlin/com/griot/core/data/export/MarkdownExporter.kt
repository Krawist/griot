package com.griot.core.data.export

import com.griot.core.data.model.ExportFile

/** Exports the songs as Markdown, to share or to keep elsewhere. */
interface MarkdownExporter {
    /** The song [songId] as a Markdown file signed by the author; `null` when it does not exist. */
    suspend fun exportSong(songId: String): ExportFile?

    /** Every song out of the trash, one Markdown file each, in a zip. */
    suspend fun exportAll(): ExportFile

    /** Copies [file] to the document [uri] the user picked; `false` when it could not be written. */
    suspend fun save(file: ExportFile, uri: String): Boolean
}
