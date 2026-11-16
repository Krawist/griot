package com.griot.core.data.song

import com.griot.core.data.model.NewSection
import com.griot.core.data.model.PastedSections
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * In-memory [SongEditorRepository] for tests: [document] is the song it shows; [calls] records every change asked,
 * such as `"rename my-section-id my-name"`, in order.
 */
class FakeSongEditorRepository(document: SongDocument? = null) : SongEditorRepository {
    val document = MutableStateFlow(document)

    val calls = mutableListOf<String>()

    override fun observeDocument(songId: String) = document

    override suspend fun setLanguage(songId: String, language: SongLanguage) {
        calls += "language $songId $language"
    }

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

    override suspend fun splitVerse(verseId: String, lines: List<String>): List<String> {
        calls += "split $verseId ${lines.joinToString(separator = "|")}"
        return lines.drop(1).indices.map { "split-verse-id-${it + 1}" }
    }

    override suspend fun mergeVerses(previousId: String, verseId: String) {
        calls += "merge $previousId $verseId"
    }

    override suspend fun closeSection(verseId: String): String {
        calls += "close $verseId"
        return "closed-verse-id"
    }

    override suspend fun pasteSections(verseId: String, sections: List<NewSection>): PastedSections {
        calls += "paste $verseId ${sections.size}"
        return PastedSections(sectionIds = listOf("pasted-section-id"), verseIds = listOf("pasted-verse-id"))
    }

    override suspend fun replaceContent(songId: String, sections: List<NewSection>) {
        calls += "replace $songId ${sections.size}"
    }

    override suspend fun undoPaste(paste: PastedSections, verseId: String, lines: List<String>) {
        calls += "undo paste $verseId ${lines.joinToString(separator = "|")}"
    }
}
