package com.griot.core.data.organisation

import com.griot.core.data.database.entity.GenreEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongGenreEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.Genre
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database: the genres are checked as they are read back.
internal class GenreRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC)
    private val repository = GenreRepositoryImpl(database = database, clock = clock)

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveGenres {
        @Test
        fun `should list the genres in order`() = runTest {
            // given
            database.genreDao().insert(
                GenreEntity(
                    id = "my-genre-id",
                    name = "my-name",
                    position = 1,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.genreDao().insert(
                GenreEntity(
                    id = "afro",
                    name = "",
                    builtIn = BuiltInGenre.AFRO,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            val genres = repository.observeGenres().first()

            // then
            genres shouldContainExactly listOf(
                Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO),
                Genre(id = "my-genre-id", name = "my-name", builtIn = null),
            )
        }
    }

    @Nested
    inner class CreateGenre {
        @Test
        fun `should create the genre after the others`() = runTest {
            // given
            database.genreDao().insert(
                GenreEntity(
                    id = "afro",
                    name = "",
                    builtIn = BuiltInGenre.AFRO,
                    position = 5,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            val id = repository.createGenre(" my-name ")

            // then
            database.genreDao().getById(id) shouldBe GenreEntity(
                id = id,
                name = "my-name",
                position = 6,
                updatedAt = Instant.parse("2026-11-17T10:00:00Z"),
            )
        }
    }

    @Nested
    inner class RenameGenre {
        @Test
        fun `should rename a genre of the author`() = runTest {
            // given
            database.genreDao().insert(
                GenreEntity(
                    id = "my-genre-id",
                    name = "my-name-1",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            repository.renameGenre(id = "my-genre-id", name = "my-name-2")

            // then
            database.genreDao().getById("my-genre-id")?.name shouldBe "my-name-2"
        }

        @Test
        fun `should keep the name of a built-in genre`() = runTest {
            // given
            val afro = GenreEntity(
                id = "afro",
                name = "",
                builtIn = BuiltInGenre.AFRO,
                position = 0,
                updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
            )
            database.genreDao().insert(afro)

            // when
            repository.renameGenre(id = "afro", name = "my-name")

            // then
            database.genreDao().getById("afro") shouldBe afro
        }
    }

    @Nested
    inner class DeleteGenre {
        @Test
        fun `should delete the genre and remove it from its songs`() = runTest {
            // given
            database.genreDao().insert(
                GenreEntity(
                    id = "my-genre-id",
                    name = "my-name",
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.songDao().insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.songGenreDao().insert(
                SongGenreEntity(
                    id = "my-song-id:my-genre-id",
                    songId = "my-song-id",
                    genreId = "my-genre-id",
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            repository.deleteGenre("my-genre-id")

            // then
            database.genreDao().getById("my-genre-id")?.deleted shouldBe true
            database.songGenreDao().getById("my-song-id:my-genre-id")?.let { it.deleted to it.updatedAt } shouldBe
                (true to Instant.parse("2026-11-17T10:00:00Z"))
        }

        @Test
        fun `should keep a built-in genre`() = runTest {
            // given
            val afro = GenreEntity(
                id = "afro",
                name = "",
                builtIn = BuiltInGenre.AFRO,
                position = 0,
                updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
            )
            database.genreDao().insert(afro)

            // when
            repository.deleteGenre("afro")

            // then
            database.genreDao().getById("afro") shouldBe afro
        }
    }

    @Nested
    inner class TagSongs {
        @Test
        fun `should tag the songs with the genres added`() = runTest {
            // when
            repository.tagSongs(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                add = setOf("my-genre-id"),
                remove = emptySet(),
            )

            // then
            database.songGenreDao().getById("my-song-id-2:my-genre-id") shouldBe SongGenreEntity(
                id = "my-song-id-2:my-genre-id",
                songId = "my-song-id-2",
                genreId = "my-genre-id",
                updatedAt = Instant.parse("2026-11-17T10:00:00Z"),
            )
            database.songGenreDao().getById("my-song-id-1:my-genre-id")?.deleted shouldBe false
        }

        @Test
        fun `should take the genres removed off the songs and keep the others`() = runTest {
            // given
            listOf("my-genre-id-1", "my-genre-id-2").forEach { genreId ->
                database.songGenreDao().insert(
                    SongGenreEntity(
                        id = "my-song-id:$genreId",
                        songId = "my-song-id",
                        genreId = genreId,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                )
            }

            // when
            repository.tagSongs(songIds = listOf("my-song-id"), add = emptySet(), remove = setOf("my-genre-id-1"))

            // then
            database.songGenreDao().getById("my-song-id:my-genre-id-1")?.deleted shouldBe true
            database.songGenreDao().getById("my-song-id:my-genre-id-2")?.deleted shouldBe false
        }
    }
}
