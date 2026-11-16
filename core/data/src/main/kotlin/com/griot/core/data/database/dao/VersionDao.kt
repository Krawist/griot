package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.griot.core.data.database.entity.VersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface VersionDao {
    /** Records [version]; versions are never updated, only added. */
    @Insert
    suspend fun insert(version: VersionEntity)

    /** The versions of the verse [verseId] that are not deleted, newest first. */
    @Query("SELECT * FROM versions WHERE verseId = :verseId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeByVerse(verseId: String): Flow<List<VersionEntity>>

    /** The versions of the whole song [songId] that are not deleted, newest first. */
    @Query("SELECT * FROM versions WHERE songId = :songId AND verseId IS NULL AND deleted = 0 ORDER BY createdAt DESC")
    fun observeBySong(songId: String): Flow<List<VersionEntity>>
}
