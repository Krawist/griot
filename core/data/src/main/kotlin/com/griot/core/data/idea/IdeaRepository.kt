package com.griot.core.data.idea

import com.griot.core.data.model.Idea
import kotlinx.coroutines.flow.Flow

/** The ideas box: sentences, titles and images saved in a few seconds, free or linked to a song. */
interface IdeaRepository {
    /** The ideas, newest first. */
    fun observeIdeas(): Flow<List<Idea>>

    /** The ideas linked to the song [songId], newest first. */
    fun observeSongIdeas(songId: String): Flow<List<Idea>>

    /** Saves a text idea, free, and returns its id. */
    suspend fun addIdea(text: String): String

    /** Copies the image at [uri] into the app's files and saves it as a free idea; `null` when it cannot be read. */
    suspend fun addImageIdea(uri: String): String?

    suspend fun updateIdea(id: String, text: String)

    /** Links the idea to the song [songId]; `null` frees it. */
    suspend fun linkIdea(id: String, songId: String?)

    /** Starts a new draft from the idea: its text is the first verse, and the idea is linked to it. Returns its id. */
    suspend fun startSong(id: String): String

    suspend fun deleteIdea(id: String)

    suspend fun restoreIdea(id: String)
}
