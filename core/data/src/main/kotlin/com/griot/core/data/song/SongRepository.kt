package com.griot.core.data.song

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.TrashedSong
import kotlinx.coroutines.flow.Flow

/** The songs, written to the phone first; sync sends them to the other devices. */
interface SongRepository {
    /** The songs out of the trash, last changed first. */
    fun observeLibrary(): Flow<List<LibrarySong>>

    /** The songs in the trash, last deleted first. */
    fun observeTrash(): Flow<List<TrashedSong>>

    /**
     * Creates an empty draft in the writing language of new songs, with a first verse section holding one empty
     * verse, and returns its id.
     */
    suspend fun createSong(): String

    /**
     * Creates a song in progress named [title], in automatic language, holding [sections] (a repeat pointing to its
     * source by index), and returns its id.
     */
    suspend fun importSong(title: String, sections: List<NewSection>): String

    /** Gives the song [id] the [title]. */
    suspend fun renameSong(id: String, title: String)

    /**
     * Copies the song [id] under [title], with its sections and verses (repeats point to the copied sections), and
     * returns the id of the copy. History and ideas stay with the original.
     */
    suspend fun duplicateSong(id: String, title: String): String

    /** Moves the song [id] to the trash; it is removed for good after [TRASH_DAYS] days. */
    suspend fun deleteSong(id: String)

    /** Takes the song [id] out of the trash. */
    suspend fun restoreSong(id: String)

    /** Removes for good the songs in the trash for more than [TRASH_DAYS] days. */
    suspend fun purgeTrash()

    companion object {
        /** Days a deleted song stays in the trash. */
        const val TRASH_DAYS = 30L
    }
}
