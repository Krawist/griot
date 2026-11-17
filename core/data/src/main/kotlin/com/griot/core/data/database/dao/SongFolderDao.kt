package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SongFolderEntity
import java.time.Instant

@Dao
internal interface SongFolderDao {
    @Insert
    suspend fun insert(row: SongFolderEntity)

    @Update
    suspend fun update(row: SongFolderEntity)

    /** Writes the row, replacing the one with the same id: filing a song again takes it back. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: SongFolderEntity)

    @Query("SELECT * FROM song_folders WHERE id = :id")
    suspend fun getById(id: String): SongFolderEntity?

    /** Takes the songs [songIds] out of the folder [folderId] at [at]. */
    @Query(
        """
        UPDATE song_folders SET deleted = 1, updatedAt = :at
        WHERE folderId = :folderId AND songId IN (:songIds) AND deleted = 0
        """,
    )
    suspend fun remove(songIds: List<String>, folderId: String, at: Instant)

    /** Takes every song out of the folder [folderId] at [at]. */
    @Query("UPDATE song_folders SET deleted = 1, updatedAt = :at WHERE folderId = :folderId AND deleted = 0")
    suspend fun removeFolder(folderId: String, at: Instant)
}
