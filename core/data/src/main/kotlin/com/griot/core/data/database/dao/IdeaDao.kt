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

    /** The ideas that are not deleted, newest first, each with the title of its song when it is linked to one. */
    @Query(
        """
        SELECT i.id, i.text, i.imagePath, s.id AS songId, s.title AS songTitle, i.createdAt
        FROM ideas i LEFT JOIN songs s ON i.songId = s.id AND s.deleted = 0
        WHERE i.deleted = 0
        ORDER BY i.createdAt DESC
        """,
    )
    fun observeWithSongs(): Flow<List<IdeaRow>>

    /** Changes the text of the idea at [at]. */
    @Query("UPDATE ideas SET text = :text, updatedAt = :at WHERE id = :id")
    suspend fun updateText(id: String, text: String, at: Instant)

    /** Links the idea to the song [songId] at [at]; `null` frees it. */
    @Query("UPDATE ideas SET songId = :songId, updatedAt = :at WHERE id = :id")
    suspend fun link(id: String, songId: String?, at: Instant)

    /** Takes the idea out of the deleted ones at [at]. */
    @Query("UPDATE ideas SET deleted = 0, updatedAt = :at WHERE id = :id")
    suspend fun restore(id: String, at: Instant)

    /** The idea [id], deleted or not; `null` when there is none. */
    @Query("SELECT * FROM ideas WHERE id = :id")
    suspend fun getById(id: String): IdeaEntity?

    /** Marks the idea deleted at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE ideas SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
