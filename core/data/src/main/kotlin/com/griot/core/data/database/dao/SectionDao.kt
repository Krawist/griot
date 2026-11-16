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

    /** Marks deleted at [at] the repeats of the section [id]: they show nothing without it. */
    @Query("UPDATE sections SET deleted = 1, updatedAt = :at WHERE repeatOfSectionId = :id AND deleted = 0")
    suspend fun markRepeatsDeleted(id: String, at: Instant)

    /** Moves the section to [position] at [at]. */
    @Query("UPDATE sections SET position = :position, updatedAt = :at WHERE id = :id")
    suspend fun move(id: String, position: Int, at: Instant)

    /** Names the section at [at]; `null` gives it back its automatic label. */
    @Query("UPDATE sections SET name = :name, updatedAt = :at WHERE id = :id")
    suspend fun rename(id: String, name: String?, at: Instant)
}
