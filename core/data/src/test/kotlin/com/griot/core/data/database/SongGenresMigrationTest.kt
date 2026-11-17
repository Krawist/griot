package com.griot.core.data.database

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.io.File
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

// A database of version 9, built from its exported schema, opened by the app: Room runs the migrations up to the last.
internal class SongGenresMigrationTest {
    @TempDir
    lateinit var directory: File

    @Nested
    inner class Migrate {
        @Test
        fun `should keep the genre each song had`() = runTest {
            // given
            val file = File(directory, "griot.db")
            createVersion9(file)
            BundledSQLiteDriver().open(file.path).use { connection ->
                connection.execSQL(
                    """
                    INSERT INTO songs (id, title, language, languageDetected, status, genreId, createdAt, updatedAt,
                        deleted)
                    VALUES ('my-song-id-1', 'my-title', 'FRENCH', 0, 'DRAFT', 'rap', 1000, 2000, 0),
                        ('my-song-id-2', 'my-title', 'FRENCH', 0, 'DRAFT', NULL, 1000, 2000, 0)
                    """,
                )
            }

            // when
            val database = Room.databaseBuilder(FileContext(file), GriotDatabase::class.java, file.name)
                .setDriver(BundledSQLiteDriver())
                .build()
            val genres = database.songGenreDao().genresOf("my-song-id-1")
            val none = database.songGenreDao().genresOf("my-song-id-2")
            database.close()

            // then
            genres shouldContainExactly listOf("rap")
            none shouldBe emptyList()
        }
    }

    // The tables, indices and identity of version 9, as its exported schema writes them.
    private fun createVersion9(file: File) {
        val schema = Json.parseToJsonElement(File(SCHEMA_9).readText()).jsonObject.getValue("database").jsonObject
        BundledSQLiteDriver().open(file.path).use { connection ->
            schema.getValue("entities").jsonArray.map { it.jsonObject }.forEach { entity ->
                val table = entity.getValue("tableName").jsonPrimitive.content
                val statements = listOf(entity.getValue("createSql").jsonPrimitive.content) +
                    entity["indices"]?.jsonArray.orEmpty().map {
                        it.jsonObject.getValue("createSql").jsonPrimitive.content
                    }
                statements.forEach { connection.execSQL(it.replace("\${TABLE_NAME}", table)) }
            }
            schema.getValue("setupQueries").jsonArray.forEach { connection.execSQL(it.jsonPrimitive.content) }
            connection.execSQL("PRAGMA user_version = 9")
        }
    }

    // A context that only knows where the database file is: unit tests run without Android.
    private class FileContext(private val file: File) : ContextWrapper(null) {
        override fun getDatabasePath(name: String): File = file

        override fun getApplicationContext(): Context = this
    }

    private companion object {
        const val SCHEMA_9 = "schemas/com.griot.core.data.database.GriotDatabase/9.json"
    }
}
