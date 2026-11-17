package com.griot.core.data.database

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

/**
 * Version 5 to 6: a song can be in several folders, through `song_folders`; its own `folderId` goes. No song was filed
 * yet (filing came after), so nothing is lost.
 */
@DeleteColumn(tableName = "songs", columnName = "folderId")
internal class SongFoldersMigration : AutoMigrationSpec
