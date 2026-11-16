package com.griot.core.data.database.dao

import android.database.SQLException
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.assertions.throwables.shouldThrow
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

internal class SectionDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao
    private lateinit var sectionDao: SectionDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
        sectionDao = database.sectionDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Insert {
        @Test
        fun `should keep a repeated chorus pointing to its source`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-1",
                    songId = "my-song-id",
                    kind = SectionKind.CHORUS,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val repeat = SectionEntity(
                id = "my-section-id-2",
                songId = "my-song-id",
                kind = SectionKind.CHORUS,
                position = 1,
                repeatOfSectionId = "my-section-id-1",
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )

            // when
            sectionDao.insert(repeat)

            // then
            sectionDao.getById("my-section-id-2") shouldBe repeat
        }

        @Test
        fun `should refuse a repeat of a section that does not exist`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val repeat = SectionEntity(
                id = "my-section-id",
                songId = "my-song-id",
                kind = SectionKind.CHORUS,
                position = 0,
                repeatOfSectionId = "my-repeat-of-section-id",
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )

            // when
            // Room rethrows the foreign key error as an Android SQLException, whose message the unit test stubs drop.
            shouldThrow<SQLException> { sectionDao.insert(repeat) }

            // then
            sectionDao.getById("my-section-id").shouldBeNull()
        }
    }

    @Nested
    inner class ObserveBySong {
        @Test
        fun `should list the sections of the song in order`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val second = SectionEntity(
                id = "my-section-id-1",
                songId = "my-song-id",
                kind = SectionKind.CHORUS,
                position = 1,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val first = SectionEntity(
                id = "my-section-id-2",
                songId = "my-song-id",
                kind = SectionKind.VERSE,
                name = "my-name",
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            sectionDao.insert(second)
            sectionDao.insert(first)

            // when
            val sections = sectionDao.observeBySong("my-song-id").first()

            // then
            sections shouldContainExactly listOf(first, second)
        }

        @Test
        fun `should leave out the deleted sections`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val kept = SectionEntity(
                id = "my-section-id-1",
                songId = "my-song-id",
                kind = SectionKind.VERSE,
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            sectionDao.insert(kept)
            sectionDao.insert(
                SectionEntity(
                    id = "my-section-id-2",
                    songId = "my-song-id",
                    kind = SectionKind.BRIDGE,
                    position = 1,
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    deleted = true,
                ),
            )

            // when
            val sections = sectionDao.observeBySong("my-song-id").first()

            // then
            sections shouldContainExactly listOf(kept)
        }
    }

    @Nested
    inner class MarkDeleted {
        @Test
        fun `should keep the row flagged as deleted with the time of the delete`() = runTest {
            // given
            songDao.insert(
                SongEntity(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            val section = SectionEntity(
                id = "my-section-id",
                songId = "my-song-id",
                kind = SectionKind.OUTRO,
                position = 0,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            sectionDao.insert(section)

            // when
            sectionDao.markDeleted(id = "my-section-id", at = Instant.parse("2026-11-16T12:00:00Z"))

            // then
            sectionDao.getById("my-section-id") shouldBe section.copy(
                deleted = true,
                updatedAt = Instant.parse("2026-11-16T12:00:00Z"),
            )
        }
    }
}
