package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.IdeaEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface IdeaDao {
    @Insert
    suspend fun insert(idea: IdeaEntity)

    @Update
    suspend fun update(idea: IdeaEntity)

    /** The ideas that are not deleted, newest first. */
    @Query("SELECT * FROM ideas WHERE deleted = 0 ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<IdeaEntity>>

    /** The ideas linked to the song [songId] that are not deleted, newest first. */
    @Query("SELECT * FROM ideas WHERE songId = :songId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeBySong(songId: String): Flow<List<IdeaEntity>>

    /** Marks the idea deleted at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE ideas SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
