package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.VerseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface VerseDao {
    @Insert
    suspend fun insert(verse: VerseEntity)

    @Update
    suspend fun update(verse: VerseEntity)

    /** The verse with [id], deleted or not; `null` when there is none. */
    @Query("SELECT * FROM verses WHERE id = :id")
    suspend fun getById(id: String): VerseEntity?

    /**
     * The verses shown in the section [sectionId] that are not deleted, in order: for a repeat, those of the section
     * it repeats.
     */
    @Query(
        """
        SELECT * FROM verses
        WHERE sectionId = (SELECT COALESCE(repeatOfSectionId, id) FROM sections WHERE id = :sectionId)
            AND deleted = 0
        ORDER BY position
        """,
    )
    fun observeBySection(sectionId: String): Flow<List<VerseEntity>>

    /** The verses of the song [songId] that are not deleted, in sections that are not deleted. */
    @Query(
        """
        SELECT v.* FROM verses v JOIN sections c ON v.sectionId = c.id
        WHERE c.songId = :songId AND c.deleted = 0 AND v.deleted = 0
        ORDER BY c.position, v.position
        """,
    )
    suspend fun getBySong(songId: String): List<VerseEntity>

    /** Marks the verse deleted at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE verses SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
