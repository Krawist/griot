package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.SongLanguage
import java.time.Instant
import kotlinx.coroutines.flow.Flow

// One query per need of the library, the trash and the editor.
@Suppress("TooManyFunctions")
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

    /** The song [id], in the trash or not; `null` when there is none. */
    @Query("SELECT * FROM songs WHERE id = :id")
    fun observeById(id: String): Flow<SongEntity?>

    /** Records a change of the song or of its content at [at], for the library's order and sync. */
    @Query("UPDATE songs SET updatedAt = :at WHERE id = :id")
    suspend fun touch(id: String, at: Instant)

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

    /**
     * The verses of the first section of every song out of the trash, in order (those of its source for a repeat):
     * the library shows their rhyme scheme.
     */
    @Query(
        """
        SELECT c.songId AS songId, v.text AS text
        FROM sections c JOIN songs s ON s.id = c.songId
        JOIN verses v ON v.sectionId = COALESCE(c.repeatOfSectionId, c.id)
        WHERE s.deleted = 0 AND c.deleted = 0 AND v.deleted = 0
            AND c.position = (SELECT MIN(f.position) FROM sections f WHERE f.songId = c.songId AND f.deleted = 0)
        ORDER BY c.songId, v.position
        """,
    )
    fun observeFirstSections(): Flow<List<FirstSectionVerseRow>>

    /** The songs in the trash, last deleted first. */
    @Query("SELECT id, title, updatedAt AS deletedAt FROM songs WHERE deleted = 1 ORDER BY updatedAt DESC")
    fun observeTrash(): Flow<List<TrashedSongRow>>

    /** Renames the song at [at]. */
    @Query("UPDATE songs SET title = :title, updatedAt = :at WHERE id = :id")
    suspend fun rename(id: String, title: String, at: Instant)

    /** Sets the writing language of the song at [at]. */
    @Query("UPDATE songs SET language = :language, languageDetected = :detected, updatedAt = :at WHERE id = :id")
    suspend fun setLanguage(id: String, language: SongLanguage, detected: Boolean, at: Instant)

    /** The songs out of the trash whose language is detected from their lyrics. */
    @Query("SELECT * FROM songs WHERE languageDetected = 1 AND deleted = 0")
    suspend fun getDetected(): List<SongEntity>

    /** Takes the song out of the trash at [at]. */
    @Query("UPDATE songs SET deleted = 0, updatedAt = :at WHERE id = :id")
    suspend fun restore(id: String, at: Instant)

    /** Removes for good the songs deleted before [before], with their sections, verses and versions. */
    @Query("DELETE FROM songs WHERE deleted = 1 AND updatedAt < :before")
    suspend fun purgeDeletedBefore(before: Instant)

    /** Moves the song to the trash at [at]; the row stays so that the delete travels to other devices. */
    @Query("UPDATE songs SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
