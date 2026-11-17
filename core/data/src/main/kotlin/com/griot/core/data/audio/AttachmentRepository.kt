package com.griot.core.data.audio

import com.griot.core.data.model.Attachment
import kotlinx.coroutines.flow.Flow

/** The audios of the songs: memos, files of the phone (their address only) and links, synced like the songs. */
interface AttachmentRepository {
    /** The audios of the song [songId], newest first. */
    fun observeAttachments(songId: String): Flow<List<Attachment>>

    /** Adds the file [uri] picked by the author to the song [songId]; `null` when it cannot be read. */
    suspend fun addFile(songId: String, uri: String): String?

    /** Adds the link [url] to the song [songId], named [title] (or after its address when blank); returns its id. */
    suspend fun addLink(songId: String, url: String, title: String): String

    /** Points the file [id], moved or on another phone, to the file [uri] picked again; `false` when unreadable. */
    suspend fun relink(id: String, uri: String): Boolean

    /** Renames the audio [id]. */
    suspend fun rename(id: String, title: String)

    /** Removes the audio [id] from its song; the file itself stays where it is. */
    suspend fun delete(id: String)
}
