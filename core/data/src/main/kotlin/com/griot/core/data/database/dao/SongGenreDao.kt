package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.SongGenreEntity
import java.time.Instant

@Dao
internal interface SongGenreDao {
    @Insert
    suspend fun insert(row: SongGenreEntity)

    @Update
    suspend fun update(row: SongGenreEntity)

    /** Writes the row, replacing the one with the same id: tagging a song again takes the genre back. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: SongGenreEntity)

    @Query("SELECT * FROM song_genres WHERE id = :id")
    suspend fun getById(id: String): SongGenreEntity?

    /** The genres of the song [songId]. */
    @Query("SELECT genreId FROM song_genres WHERE songId = :songId AND deleted = 0")
    suspend fun genresOf(songId: String): List<String>

    /** Takes the genre [genreId] off the songs [songIds] at [at]. */
    @Query(
        """
        UPDATE song_genres SET deleted = 1, updatedAt = :at
        WHERE genreId = :genreId AND songId IN (:songIds) AND deleted = 0
        """,
    )
    suspend fun remove(songIds: List<String>, genreId: String, at: Instant)

    /** Takes the genre [genreId] off every song at [at]. */
    @Query("UPDATE song_genres SET deleted = 1, updatedAt = :at WHERE genreId = :genreId AND deleted = 0")
    suspend fun removeGenre(genreId: String, at: Instant)
}
