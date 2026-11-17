package com.griot.core.data.database

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/** Version 10 to 11: the genres of a song are in `song_genres` (copied in version 10); its own `genreId` goes. */
@DeleteColumn(tableName = "songs", columnName = "genreId")
internal class SongGenreColumnMigration : AutoMigrationSpec
