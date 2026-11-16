package com.griot.core.data.database.dao

import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.VersionSource
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class VersionDaoTest {
    private lateinit var database: GriotDatabase
    private lateinit var songDao: SongDao
    private lateinit var versionDao: VersionDao

    @BeforeEach
    fun setUp() {
        database = inMemoryDatabase()
        songDao = database.songDao()
        versionDao = database.versionDao()
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveByVerse {
        @Test
        fun `should list the versions of the verse newest first`() = runTest {
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
            val older = VersionEntity(
                id = "my-version-id-1",
                songId = "my-song-id",
                verseId = "my-verse-id",
                text = "my-text-1",
                source = VersionSource.AUTHOR,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            val newer = VersionEntity(
                id = "my-version-id-2",
                songId = "my-song-id",
                verseId = "my-verse-id",
                text = "my-text-2",
                source = VersionSource.AI_SUGGESTION,
                createdAt = Instant.parse("2026-11-16T11:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
            )
            versionDao.insert(older)
            versionDao.insert(newer)

            // when
            val versions = versionDao.observeByVerse("my-verse-id").first()

            // then
            versions shouldContainExactly listOf(newer, older)
        }
    }

    @Nested
    inner class ObserveBySong {
        @Test
        fun `should list only the versions of the whole song`() = runTest {
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
            val songVersion = VersionEntity(
                id = "my-version-id-1",
                songId = "my-song-id",
                verseId = null,
                text = "my-text-1",
                source = VersionSource.AUTHOR,
                createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
            )
            versionDao.insert(songVersion)
            versionDao.insert(
                VersionEntity(
                    id = "my-version-id-2",
                    songId = "my-song-id",
                    verseId = "my-verse-id",
                    text = "my-text-2",
                    source = VersionSource.AUTHOR,
                    createdAt = Instant.parse("2026-11-16T11:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T11:00:00Z"),
                ),
            )

            // when
            val versions = versionDao.observeBySong("my-song-id").first()

            // then
            versions shouldContainExactly listOf(songVersion)
        }
    }
}
