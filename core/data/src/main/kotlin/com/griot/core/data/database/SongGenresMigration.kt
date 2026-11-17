package com.griot.core.data.database

import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

/**
 * Version 9 to 10: a song can have several genres, through `song_genres`, and favourites have their table. The genre
 * each song has is copied there, dated like the song; `songs.genreId` goes in version 11.
 */
internal class SongGenresMigration : AutoMigrationSpec {
    override fun onPostMigrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            INSERT OR IGNORE INTO song_genres (id, songId, genreId, updatedAt, deleted)
            SELECT id || ':' || genreId, id, genreId, updatedAt, 0 FROM songs WHERE genreId IS NOT NULL
            """,
        )
    }
}
