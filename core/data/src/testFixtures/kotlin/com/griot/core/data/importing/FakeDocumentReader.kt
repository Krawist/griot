package com.griot.core.data.importing

import com.griot.core.data.model.ImportedText

/** [DocumentReader] for tests: it reads the texts of [files], by uri. */
class FakeDocumentReader(private val files: Map<String, ImportedText> = emptyMap()) : DocumentReader {
    override suspend fun read(uris: List<String>): List<ImportedText> = uris.mapNotNull { files[it] }
}
