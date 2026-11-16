package com.griot.core.data.importing

import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText

/** Turns texts written elsewhere (Samsung Notes, files, a paste) into songs. */
interface TextImporter {
    /**
     * Creates one song per text, split into sections as a paste is. An empty or unreadable text is reported by its
     * name and does not stop the others.
     */
    suspend fun import(texts: List<ImportedText>): ImportResult
}
