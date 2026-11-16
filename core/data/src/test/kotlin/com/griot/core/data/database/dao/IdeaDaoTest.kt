package com.griot.core.data.database.dao

import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class IdeaDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao
    private lateinit var ideaDao: IdeaDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
        ideaDao = database.ideaDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveAll {
        @Test
        fun `should list the ideas newest first`() = runTest {
            // given
            val older = IdeaEntity(
                id = "my-idea-id-1",
                text = "my-text-1",
                createdAt = Instant.parse("2026-11-15T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-15T10:00:00Z"),
            )
            val newer = IdeaEntity(
                id = "my-idea-id-2",
                text = "my-text-2",
                imagePath = "my-image-path",
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            ideaDao.insert(older)
            ideaDao.insert(newer)

            // when
            val ideas = ideaDao.observeAll().first()

            // then
            ideas shouldContainExactly listOf(newer, older)
        }

        @Test
        fun `should leave out the deleted ideas`() = runTest {
            // given
            ideaDao.insert(
                IdeaEntity(
                    id = "my-idea-id",
                    text = "my-text",
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )

            // when
            ideaDao.markDeleted(id = "my-idea-id", at = Instant.parse("2026-11-16T12:00:00Z"))

            // then
            ideaDao.observeAll().first().shouldBeEmpty()
        }
    }

    @Nested
    inner class ObserveBySong {
        @Test
        fun `should list only the ideas linked to the song`() = runTest {
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
            val linked = IdeaEntity(
                id = "my-idea-id-1",
                text = "my-text-1",
                songId = "my-song-id",
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            ideaDao.insert(linked)
            ideaDao.insert(
                IdeaEntity(
                    id = "my-idea-id-2",
                    text = "my-text-2",
                    createdAt = Instant.parse("2026-11-16T11:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                ),
            )

            // when
            val ideas = ideaDao.observeBySong("my-song-id").first()

            // then
            ideas shouldContainExactly listOf(linked)
        }
    }
}
