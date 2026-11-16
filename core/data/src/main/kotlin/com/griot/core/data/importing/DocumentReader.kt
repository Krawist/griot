package com.griot.core.data.importing

import com.griot.core.data.model.ImportedText

/** Reads the text files the user picked. */
interface DocumentReader {
    /** The files at [uris], in order, each named after its file without extension; unreadable ones without text. */
    suspend fun read(uris: List<String>): List<ImportedText>
}
