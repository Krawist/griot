package com.griot.core.data.importing

import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText

/** [TextImporter] for tests: it gives [result] and records the texts it was asked to import. */
class FakeTextImporter(var result: ImportResult = ImportResult(songs = 0, sections = 0, failed = emptyList())) :
    TextImporter {
    val imported = mutableListOf<ImportedText>()

    override suspend fun import(texts: List<ImportedText>): ImportResult {
        imported += texts
        return result
    }
}
