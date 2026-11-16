package com.griot.core.data.song

import com.griot.core.data.model.SongVersion
import com.griot.core.data.model.VerseVersion
import com.griot.core.data.model.VersionSource
import kotlinx.coroutines.flow.Flow

/** The past texts of verses and songs: recorded as the author writes, never overwritten, thinned out over time. */
interface VersionRepository {
    /** The versions of the verse [verseId], newest first. */
    fun observeVerseHistory(verseId: String): Flow<List<VerseVersion>>

    /** The versions of the whole song [songId], newest first. */
    fun observeSongHistory(songId: String): Flow<List<SongVersion>>

    /** Records the text of the verse [verseId] as a version from [source], unless it is the last version already. */
    suspend fun recordVerse(verseId: String, source: VersionSource = VersionSource.AUTHOR)

    /**
     * Records the whole song [songId] as a version, at most every [SONG_INTERVAL_MINUTES] minutes and only when it
     * changed since the last one.
     */
    suspend fun recordSong(songId: String)

    /** Gives the verse the text of the version [versionId], as a new version: its text before is kept first. */
    suspend fun restoreVerse(versionId: String)

    /** Gives the song the content of the version [versionId], as a new version: its content before is kept first. */
    suspend fun restoreSong(versionId: String)

    /**
     * Thins out the versions older than [KEEP_ALL_DAYS] days: of each verse and of each song, the last one of each
     * day is kept.
     */
    suspend fun thin()

    companion object {
        /** Least time between two versions of a whole song, in minutes. */
        const val SONG_INTERVAL_MINUTES = 5L

        /** Days during which every version is kept. */
        const val KEEP_ALL_DAYS = 7L
    }
}
