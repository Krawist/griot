package com.griot.core.data.audio

import com.griot.core.data.model.Attachment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory [AttachmentRepository] for tests: [attachments] is what it lists, for every song; [calls] records every
 * change asked, such as `"link my-song-id my-url my-title"`, in order. A file whose address starts with « bad » cannot
 * be read.
 */
class FakeAttachmentRepository(attachments: List<Attachment> = emptyList()) : AttachmentRepository {
    val attachments = MutableStateFlow(attachments)

    val calls = mutableListOf<String>()

    override fun observeAttachments(songId: String) = attachments.map { list -> list.filter { it.songId == songId } }

    override suspend fun addFile(songId: String, uri: String): String? {
        calls += "file $songId $uri"
        return "added-attachment-id".takeUnless { uri.startsWith("bad") }
    }

    override suspend fun addMemo(
        songId: String,
        memo: RecordedMemo,
        title: String,
        sectionId: String?,
        verseId: String?,
    ): String {
        calls += "memo $songId ${memo.uri} ${memo.durationMillis} $title $sectionId $verseId"
        return "added-attachment-id"
    }

    override suspend fun addLink(songId: String, url: String, title: String): String {
        calls += "link $songId $url $title"
        return "added-attachment-id"
    }

    override suspend fun relink(id: String, uri: String): Boolean {
        calls += "relink $id $uri"
        return !uri.startsWith("bad")
    }

    override suspend fun setBeat(songId: String, id: String) {
        calls += "beat $songId $id"
    }

    override suspend fun rename(id: String, title: String) {
        calls += "rename $id $title"
    }

    override suspend fun delete(id: String) {
        calls += "delete $id"
    }
}
