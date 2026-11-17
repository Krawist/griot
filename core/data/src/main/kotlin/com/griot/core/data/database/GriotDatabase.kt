package com.griot.core.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.griot.core.data.database.dao.AttachmentDao
import com.griot.core.data.database.dao.ConflictDao
import com.griot.core.data.database.dao.FolderDao
import com.griot.core.data.database.dao.GenreDao
import com.griot.core.data.database.dao.IdeaDao
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.SongFolderDao
import com.griot.core.data.database.dao.SyncDao
import com.griot.core.data.database.dao.SyncStateDao
import com.griot.core.data.database.dao.TidyRefusalDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.dao.VersionDao
import com.griot.core.data.database.dao.WordCorrectionDao
import com.griot.core.data.database.entity.AttachmentEntity
import com.griot.core.data.database.entity.ConflictEntity
import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.database.entity.GenreEntity
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.entity.TidyRefusalEntity
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
        FolderEntity::class,
        GenreEntity::class,
        SongFolderEntity::class,
        TidyRefusalEntity::class,
        AttachmentEntity::class,
    ],
    version = 9,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = SongFoldersMigration::class),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 8, to = 9),
    ],
)
@TypeConverters(DatabaseConverters::class)
@Suppress("TooManyFunctions") // One DAO per table.
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

    abstract fun folderDao(): FolderDao

    abstract fun genreDao(): GenreDao

    abstract fun songFolderDao(): SongFolderDao

    abstract fun tidyRefusalDao(): TidyRefusalDao

    abstract fun attachmentDao(): AttachmentDao
}
