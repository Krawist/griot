package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.griot.core.data.database.entity.ConflictEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ConflictDao {
    @Insert
    suspend fun insert(conflict: ConflictEntity)

    /** The ids of the verses of the song [songId] with a conflict not looked at, oldest conflict first. */
    @Query(
        "SELECT verseId FROM sync_conflicts WHERE songId = :songId GROUP BY verseId ORDER BY MIN(createdAt), verseId",
    )
    fun observeVersesBySong(songId: String): Flow<List<String>>

    @Query("DELETE FROM sync_conflicts WHERE verseId = :verseId")
    suspend fun deleteByVerse(verseId: String)

    @Query("DELETE FROM sync_conflicts WHERE songId = :songId")
    suspend fun deleteBySong(songId: String)
}
