package com.griot.core.data.di

import android.content.Context
import androidx.room.Room
import com.griot.core.data.database.BuiltInRows
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.LanguageMigration
import com.griot.core.data.database.dao.ConflictDao
import com.griot.core.data.database.dao.FolderDao
import com.griot.core.data.database.dao.GenreDao
import com.griot.core.data.database.dao.IdeaDao
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.SyncDao
import com.griot.core.data.database.dao.SyncStateDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.dao.VersionDao
import com.griot.core.data.database.dao.WordCorrectionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("TooManyFunctions") // One provider per DAO.
@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GriotDatabase =
        Room.databaseBuilder(context, GriotDatabase::class.java, "griot.db")
            .addMigrations(LanguageMigration)
            .addCallback(BuiltInRows)
            .build()

    @Provides
    fun provideSongDao(database: GriotDatabase): SongDao = database.songDao()

    @Provides
    fun provideSectionDao(database: GriotDatabase): SectionDao = database.sectionDao()

    @Provides
    fun provideVerseDao(database: GriotDatabase): VerseDao = database.verseDao()

    @Provides
    fun provideVersionDao(database: GriotDatabase): VersionDao = database.versionDao()

    @Provides
    fun provideConflictDao(database: GriotDatabase): ConflictDao = database.conflictDao()

    @Provides
    fun provideFolderDao(database: GriotDatabase): FolderDao = database.folderDao()

    @Provides
    fun provideGenreDao(database: GriotDatabase): GenreDao = database.genreDao()

    @Provides
    fun provideIdeaDao(database: GriotDatabase): IdeaDao = database.ideaDao()

    @Provides
    fun provideSyncDao(database: GriotDatabase): SyncDao = database.syncDao()

    @Provides
    fun provideSyncStateDao(database: GriotDatabase): SyncStateDao = database.syncStateDao()

    @Provides
    fun provideWordCorrectionDao(database: GriotDatabase): WordCorrectionDao = database.wordCorrectionDao()
}
