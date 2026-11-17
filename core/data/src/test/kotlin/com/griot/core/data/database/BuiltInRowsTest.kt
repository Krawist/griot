package com.griot.core.data.database

import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.FolderKind
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class BuiltInRowsTest {
    private val database = Room.inMemoryDatabaseBuilder(ContextWrapper(null), GriotDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .addCallback(BuiltInRows)
        .build()

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Test
    fun `should write Archives and the built-in genres when the database opens`() = runTest {
        // when
        val folders = database.folderDao().observeWithCounts().first()
        val genres = database.genreDao().observeAll().first()

        // then
        folders.map { it.id to it.kind } shouldContainExactly listOf("archives" to FolderKind.ARCHIVES)
        genres.map { it.id to it.builtIn } shouldContainExactly listOf(
            "afro" to BuiltInGenre.AFRO,
            "rap" to BuiltInGenre.RAP,
            "rnb" to BuiltInGenre.RNB,
            "gospel" to BuiltInGenre.GOSPEL,
            "pop" to BuiltInGenre.POP,
            "other" to BuiltInGenre.OTHER,
        )
    }
}
