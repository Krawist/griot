package com.griot.core.data.song

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.TrashedSong
import kotlinx.coroutines.flow.Flow

/** The songs, written to the phone first; sync sends them to the other devices. */
@Suppress("TooManyFunctions") // One function per change of the library.
interface SongRepository {
    /** The songs out of the trash, last changed first. */
    fun observeLibrary(): Flow<List<LibrarySong>>

    /** The songs in the trash, last deleted first. */
    fun observeTrash(): Flow<List<TrashedSong>>

    /**
     * Creates a draft in the writing language of new songs, with a first verse section holding [firstVerse] (empty
     * by default), and returns its id.
     */
    suspend fun createSong(firstVerse: String = ""): String

    /**
     * Creates a song in progress named [title], holding [sections] (a repeat pointing to its source by index), and
     * returns its id. Its language is detected from its lyrics, or is the language of new songs when they are too
     * short to tell.
     */
    suspend fun importSong(title: String, sections: List<NewSection>): String

    /**
     * Detects again the language of every song in automatic mode, from its lyrics: at launch, so that songs stored
     * before GRI-54 or changed on another device get theirs. A song too short to tell keeps its language.
     */
    suspend fun detectLanguages()

    /** Puts the songs [ids] in the author's favourites, or takes them out when [favorite] is `false`. */
    suspend fun setFavorite(ids: List<String>, favorite: Boolean)

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
