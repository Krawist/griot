package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.model.FolderColor
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FolderDao {
    @Insert
    suspend fun insert(folder: FolderEntity)

    @Update
    suspend fun update(folder: FolderEntity)

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getById(id: String): FolderEntity?

    /** The folders that are not deleted, in order, « Archives » last, each with its count of songs. */
    @Query(
        """
        SELECT f.id, f.name, f.color, f.kind,
            (
                SELECT COUNT(*) FROM song_folders sf JOIN songs s ON s.id = sf.songId
                WHERE sf.folderId = f.id AND sf.deleted = 0 AND s.deleted = 0
            ) AS songCount
        FROM folders f
        WHERE f.deleted = 0
        ORDER BY f.kind = 'ARCHIVES', f.position
        """,
    )
    fun observeWithCounts(): Flow<List<FolderRow>>

    /** How many folders of the author there are, deleted ones included: new folders take the accents in turn. */
    @Query("SELECT COUNT(*) FROM folders WHERE kind = 'CUSTOM'")
    suspend fun countCustom(): Int

    /** The place after the last folder. */
    @Query("SELECT COALESCE(MAX(position) + 1, 0) FROM folders")
    suspend fun nextPosition(): Int

    @Query("UPDATE folders SET name = :name, updatedAt = :at WHERE id = :id")
    suspend fun rename(id: String, name: String, at: Instant)

    @Query("UPDATE folders SET color = :color, updatedAt = :at WHERE id = :id")
    suspend fun setColor(id: String, color: FolderColor, at: Instant)

    @Query("UPDATE folders SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
