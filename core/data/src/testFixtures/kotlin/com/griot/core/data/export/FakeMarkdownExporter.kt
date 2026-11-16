package com.griot.core.data.export

import com.griot.core.data.model.ExportFile

/** [MarkdownExporter] for tests: it gives [file] for any export and records what it was asked. */
class FakeMarkdownExporter(
    var file: ExportFile? = ExportFile(name = "my-name.md", mimeType = "text/markdown", path = "my-path"),
) : MarkdownExporter {
    val calls = mutableListOf<String>()

    override suspend fun exportSong(songId: String): ExportFile? {
        calls += "song $songId"
        return file
    }

    override suspend fun exportAll(): ExportFile {
        calls += "all"
        return requireNotNull(file)
    }

    override suspend fun save(file: ExportFile, uri: String): Boolean {
        calls += "save ${file.name} $uri"
        return true
    }
}
