package com.griot.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// The database version before GRI-54.
private const val LANGUAGE_MIGRATION_FROM = 3

/**
 * Version 3 to 4: « Automatique » becomes a mode, not a language (GRI-54). Songs stored as automatic are marked as
 * detected, in French until [com.griot.core.data.song.SongRepository.detectLanguages] reads their lyrics.
 */
internal object LanguageMigration : Migration(
    startVersion = LANGUAGE_MIGRATION_FROM,
    endVersion = LANGUAGE_MIGRATION_FROM + 1,
) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE songs ADD COLUMN languageDetected INTEGER NOT NULL DEFAULT 0")
        db.execSQL("UPDATE songs SET languageDetected = 1, language = 'FRENCH' WHERE language = 'AUTOMATIC'")
    }
}
