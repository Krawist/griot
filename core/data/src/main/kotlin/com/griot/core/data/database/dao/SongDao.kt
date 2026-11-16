package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SongEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Insert
    suspend fun insert(song: SongEntity)

    @Update
    suspend fun update(song: SongEntity)

    /** The song with [id], in the trash or not; `null` when there is none. */
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    /** The songs out of the trash, last changed first. */
    @Query("SELECT * FROM songs WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SongEntity>>

    /** Moves the song to the trash at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE songs SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
