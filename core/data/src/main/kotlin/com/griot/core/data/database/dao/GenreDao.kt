package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.GenreEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GenreDao {
    @Insert
    suspend fun insert(genre: GenreEntity)

    @Update
    suspend fun update(genre: GenreEntity)

    @Query("SELECT * FROM genres WHERE id = :id")
    suspend fun getById(id: String): GenreEntity?

    /** The genres that are not deleted: the built-in ones, then the author's, in order. */
    @Query("SELECT * FROM genres WHERE deleted = 0 ORDER BY position")
    fun observeAll(): Flow<List<GenreEntity>>

    /** The place after the last genre. */
    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM genres")
    suspend fun nextPosition(): Int

    @Query("UPDATE genres SET name = :name, updatedAt = :at WHERE id = :id")
    suspend fun rename(id: String, name: String, at: Instant)

    @Query("UPDATE genres SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
