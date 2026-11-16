package com.griot.core.data.database.dao

import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class GetById {
        @Test
        fun `should find a song by its id`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:05:00Z"),
            )
            songDao.insert(song)

            // when
            val found = songDao.getById("my-song-id")

            // then
            found shouldBe song
        }

        @Test
        fun `should return null for an unknown song`() = runTest {
            // when
            val found = songDao.getById("my-song-id")

            // then
            found.shouldBeNull()
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `should save the new title and change time`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title-1",
                language = SongLanguage.AUTOMATIC,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(song)
            val renamed = song.copy(title = "my-title-2", updatedAt = Instant.parse("2026-11-16T11:00:00Z"))

            // when
            songDao.update(renamed)

            // then
            songDao.getById("my-song-id") shouldBe renamed
        }
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `should list the songs last changed first`() = runTest {
            // given
            val older = SongEntity(
                id = "my-song-id-1",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-15T10:00:00Z"),
            )
            val newer = SongEntity(
                id = "my-song-id-2",
                title = "my-title-2",
                language = SongLanguage.ENGLISH,
                status = SongStatus.IN_PROGRESS,
                createdAt = Instant.parse("2026-11-02T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(older)
            songDao.insert(newer)

            // when
            val songs = songDao.observeAll().first()

            // then
            songs shouldContainExactly listOf(newer, older)
        }

        @Test
        fun `should leave out the songs in the trash`() = runTest {
            // given
            val kept = SongEntity(
                id = "my-song-id-1",
                title = "my-title-1",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val deleted = SongEntity(
                id = "my-song-id-2",
                title = "my-title-2",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                deleted = true,
            )
            songDao.insert(kept)
            songDao.insert(deleted)

            // when
            val songs = songDao.observeAll().first()

            // then
            songs shouldContainExactly listOf(kept)
        }
    }

    @Nested
    inner class MarkDeleted {
        @Test
        fun `should keep the row flagged as deleted with the time of the delete`() = runTest {
            // given
            val song = SongEntity(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            songDao.insert(song)

            // when
            songDao.markDeleted(id = "my-song-id", at = Instant.parse("2026-11-16T12:00:00Z"))

            // then
            songDao.getById("my-song-id") shouldBe song.copy(
                deleted = true,
                updatedAt = Instant.parse("2026-11-16T12:00:00Z"),
            )
        }
    }
}
