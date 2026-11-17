package com.griot.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.griot.core.data.database.dao.ConflictDao
import com.griot.core.data.database.dao.IdeaDao
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.SyncDao
import com.griot.core.data.database.dao.SyncStateDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.dao.VersionDao
import com.griot.core.data.database.dao.WordCorrectionDao
import com.griot.core.data.database.entity.ConflictEntity
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.database.entity.WordCorrectionEntity

/** Local database of Griot: everything is written here first, then synced. Schemas are exported to `schemas/`. */
@Database(
    entities = [
        SongEntity::class,
        SectionEntity::class,
        VerseEntity::class,
        VersionEntity::class,
        IdeaEntity::class,
        SyncStateEntity::class,
        WordCorrectionEntity::class,
        ConflictEntity::class,
    ],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
@TypeConverters(DatabaseConverters::class)
internal abstract class GriotDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    abstract fun sectionDao(): SectionDao

    abstract fun verseDao(): VerseDao

    abstract fun versionDao(): VersionDao

    abstract fun ideaDao(): IdeaDao

    abstract fun syncStateDao(): SyncStateDao

    abstract fun syncDao(): SyncDao

    abstract fun wordCorrectionDao(): WordCorrectionDao

    abstract fun conflictDao(): ConflictDao
}
