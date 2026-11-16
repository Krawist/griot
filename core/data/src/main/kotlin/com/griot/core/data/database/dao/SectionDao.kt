package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SectionEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SectionDao {
    @Insert
    suspend fun insert(section: SectionEntity)

    @Update
    suspend fun update(section: SectionEntity)

    /** The section with [id], deleted or not; `null` when there is none. */
    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getById(id: String): SectionEntity?

    /** The sections of the song [songId] that are not deleted, in order. */
    @Query("SELECT * FROM sections WHERE songId = :songId AND deleted = 0 ORDER BY position")
    fun observeBySong(songId: String): Flow<List<SectionEntity>>

    /** The sections of the song [songId] that are not deleted, in order. */
    @Query("SELECT * FROM sections WHERE songId = :songId AND deleted = 0 ORDER BY position")
    suspend fun getBySong(songId: String): List<SectionEntity>

    /** Marks the section deleted at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE sections SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
