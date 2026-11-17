package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.VersionEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

// Versions recorded in the same instant (the text kept just before a restore) follow their order of recording: rowid.
@Dao
internal interface VersionDao {
    /** Records [version]; versions are never updated, only added. */
    @Insert
    suspend fun insert(version: VersionEntity)

    @Update
    suspend fun update(version: VersionEntity)

    /** The versions of the verse [verseId] that are not deleted, newest first. */
    @Query("SELECT * FROM versions WHERE verseId = :verseId AND deleted = 0 ORDER BY createdAt DESC, rowid DESC")
    fun observeByVerse(verseId: String): Flow<List<VersionEntity>>

    /** The version [id]; `null` when there is none. */
    @Query("SELECT * FROM versions WHERE id = :id")
    suspend fun getById(id: String): VersionEntity?

    /** The last version of the verse [verseId] that is not deleted; `null` when there is none. */
    @Query(
        "SELECT * FROM versions WHERE verseId = :verseId AND deleted = 0 ORDER BY createdAt DESC, rowid DESC LIMIT 1",
    )
    suspend fun latestForVerse(verseId: String): VersionEntity?

    /** The last version of the whole song [songId] that is not deleted; `null` when there is none. */
    @Query(
        """
        SELECT * FROM versions WHERE songId = :songId AND verseId IS NULL AND deleted = 0
        ORDER BY createdAt DESC, rowid DESC LIMIT 1
        """,
    )
    suspend fun latestForSong(songId: String): VersionEntity?

    /** The versions recorded before [before] that are not deleted, oldest first. */
    @Query("SELECT * FROM versions WHERE createdAt < :before AND deleted = 0 ORDER BY createdAt, rowid")
    suspend fun recordedBefore(before: Instant): List<VersionEntity>

    /** Marks the versions [ids] deleted at [at]; the rows stay so that the delete travels to other devices. */
    @Query("UPDATE versions SET deleted = 1, updatedAt = :at WHERE id IN (:ids)")
    suspend fun markDeleted(ids: List<String>, at: Instant)

    /** The versions of the whole song [songId] that are not deleted, newest first. */
    @Query(
        """
        SELECT * FROM versions WHERE songId = :songId AND verseId IS NULL AND deleted = 0
        ORDER BY createdAt DESC, rowid DESC
        """,
    )
    fun observeBySong(songId: String): Flow<List<VersionEntity>>
}
