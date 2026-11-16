package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SongDao {
    @Insert
    suspend fun insert(song: SongEntity)

    /** Inserts a song with its first sections and verses, all or none. */
    @Insert
    suspend fun insertWithContent(song: SongEntity, sections: List<SectionEntity>, verses: List<VerseEntity>)

    @Update
    suspend fun update(song: SongEntity)

    /** The song with [id], in the trash or not; `null` when there is none. */
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getById(id: String): SongEntity?

    /** The songs out of the trash, last changed first. */
    @Query("SELECT * FROM songs WHERE deleted = 0 ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SongEntity>>

    /** The songs out of the trash with what the library shows of them, last changed first. */
    @Query(
        """
        SELECT s.id, s.title, s.language, s.status, s.updatedAt,
            (SELECT COUNT(*) FROM sections c WHERE c.songId = s.id AND c.deleted = 0) AS sectionCount,
            (
                SELECT v.text FROM verses v JOIN sections c ON v.sectionId = c.id
                WHERE c.songId = s.id AND c.deleted = 0 AND v.deleted = 0 AND TRIM(v.text) != ''
                ORDER BY c.position, v.position LIMIT 1
            ) AS firstVerse,
            (
                SELECT GROUP_CONCAT(v.text, char(10)) FROM verses v JOIN sections c ON v.sectionId = c.id
                WHERE c.songId = s.id AND c.deleted = 0 AND v.deleted = 0
            ) AS lyrics
        FROM songs s
        WHERE s.deleted = 0
        ORDER BY s.updatedAt DESC
        """,
    )
    fun observeLibrary(): Flow<List<LibrarySongRow>>

    /** Moves the song to the trash at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE songs SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
