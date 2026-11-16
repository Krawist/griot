package com.griot.core.data.song

import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [SongEditorRepository] for tests: [document] is the song it shows; [calls] records every change asked,
 * such as `"rename my-section-id my-name"`, in order.
 */
class FakeSongEditorRepository(document: SongDocument? = null) : SongEditorRepository {
    val document = MutableStateFlow(document)

    val calls = mutableListOf<String>()

    override fun observeDocument(songId: String) = document

    override suspend fun addSection(songId: String, kind: SectionKind): String {
        calls += "add $songId $kind"
        return "added-section-id"
    }

    override suspend fun repeatSection(sectionId: String): String {
        calls += "repeat $sectionId"
        return "repeat-section-id"
    }

    override suspend fun renameSection(sectionId: String, name: String?) {
        calls += "rename $sectionId $name"
    }

    override suspend fun moveSection(sectionId: String, offset: Int) {
        calls += "move $sectionId $offset"
    }

    override suspend fun deleteSection(sectionId: String) {
        calls += "delete $sectionId"
    }

    override suspend fun updateVerse(verseId: String, text: String) {
        calls += "verse $verseId $text"
    }
}
