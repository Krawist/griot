package com.griot.core.data.song

import com.griot.core.data.model.LibrarySong
import kotlinx.coroutines.flow.Flow

/** The songs, written to the phone first; sync sends them to the other devices. */
interface SongRepository {
    /** The songs out of the trash, last changed first. */
    fun observeLibrary(): Flow<List<LibrarySong>>

    /**
     * Creates an empty draft in the writing language of new songs, with a first verse section holding one empty
     * verse, and returns its id.
     */
    suspend fun createSong(): String
}
