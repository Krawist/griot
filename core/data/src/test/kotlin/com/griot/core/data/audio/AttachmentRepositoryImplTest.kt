package com.griot.core.data.audio

import com.griot.core.data.database.entity.AttachmentEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.Attachment
import com.griot.core.data.model.AttachmentKind
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database: the audios are checked as they are read back.
internal class AttachmentRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val files = mockk<AudioFiles>()
    private val clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC)
    private val repository = AttachmentRepositoryImpl(
        attachmentDao = database.attachmentDao(),
        files = files,
        preferences = FakeUserPreferencesRepository(),
        clock = clock,
    )

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class AddFile {
        @Test
        fun `should keep the file's address, name and length, on this phone`() = runTest {
            // given
            coEvery { files.take(any()) } returns AudioFileInfo(title = "my-title", durationMillis = 200_000)

            // when
            val id = repository.addFile(songId = "my-song-id", uri = "my-uri")

            // then
            repository.observeAttachments("my-song-id").first() shouldContainExactly listOf(
                Attachment(
                    id = id.orEmpty(),
                    songId = "my-song-id",
                    kind = AttachmentKind.FILE,
                    uri = "my-uri",
                    title = "my-title",
                    durationMillis = 200_000,
                    createdAt = Instant.parse("2026-11-17T10:00:00Z"),
                ),
            )
            coVerifySequence { files.take("my-uri") }
        }

        @Test
        fun `should add nothing for a file that cannot be read`() = runTest {
            // given
            coEvery { files.take(any()) } returns null

            // when
            val id = repository.addFile(songId = "my-song-id", uri = "my-uri")

            // then
            id.shouldBeNull()
            coVerifySequence { files.take("my-uri") }
        }
    }

    @Nested
    inner class ObserveAttachments {
        @Test
        fun `should tell a file added on another phone`() = runTest {
            // given
            database.attachmentDao().insert(
                AttachmentEntity(
                    id = "my-attachment-id",
                    songId = "my-song-id",
                    kind = AttachmentKind.FILE,
                    uri = "my-uri",
                    title = "my-title",
                    deviceId = "other-device-id",
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )

            // when
            val attachments = repository.observeAttachments("my-song-id").first()

            // then
            attachments.single().onThisPhone shouldBe false
        }
    }

    @Nested
    inner class AddMemo {
        @Test
        fun `should keep the memo on this phone, attached to its verse`() = runTest {
            // when
            val id = repository.addMemo(
                songId = "my-song-id",
                memo = RecordedMemo(uri = "my-uri", durationMillis = 24_000),
                title = "my-title",
                sectionId = "my-section-id",
                verseId = "my-verse-id",
            )

            // then
            repository.observeAttachments("my-song-id").first() shouldContainExactly listOf(
                Attachment(
                    id = id,
                    songId = "my-song-id",
                    sectionId = "my-section-id",
                    verseId = "my-verse-id",
                    kind = AttachmentKind.MEMO,
                    uri = "my-uri",
                    title = "my-title",
                    durationMillis = 24_000,
                    createdAt = Instant.parse("2026-11-17T10:00:00Z"),
                ),
            )
            verify { files wasNot Called }
        }
    }

    @Nested
    inner class AddLink {
        @Test
        fun `should name a link after its address when no name is given`() = runTest {
            // when
            repository.addLink(songId = "my-song-id", url = "www.open.spotify.com/track/1", title = " ")

            // then
            repository.observeAttachments("my-song-id").first().single().let { it.uri to it.title } shouldBe
                ("https://www.open.spotify.com/track/1" to "open.spotify.com")
            verify { files wasNot Called }
        }
    }

    @Nested
    inner class Relink {
        @Test
        fun `should point the file to the one picked again, on this phone`() = runTest {
            // given
            database.attachmentDao().insert(
                AttachmentEntity(
                    id = "my-attachment-id",
                    songId = "my-song-id",
                    kind = AttachmentKind.FILE,
                    uri = "my-uri-1",
                    title = "my-title",
                    deviceId = "other-device-id",
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
            coEvery { files.take(any()) } returns AudioFileInfo(title = "my-title", durationMillis = null)

            // when
            val relinked = repository.relink(id = "my-attachment-id", uri = "my-uri-2")

            // then
            relinked shouldBe true
            repository.observeAttachments("my-song-id").first().single().let { it.uri to it.onThisPhone } shouldBe
                ("my-uri-2" to true)
            coVerifySequence { files.take("my-uri-2") }
        }
    }

    @Nested
    inner class SetBeat {
        @Test
        fun `should make the file the song's only beat`() = runTest {
            // given
            listOf("my-attachment-id-1" to true, "my-attachment-id-2" to false).forEach { (id, beat) ->
                database.attachmentDao().insert(
                    AttachmentEntity(
                        id = id,
                        songId = "my-song-id",
                        kind = AttachmentKind.FILE,
                        uri = id,
                        title = "my-title",
                        beat = beat,
                        createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                        updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                )
            }

            // when
            repository.setBeat(songId = "my-song-id", id = "my-attachment-id-2")

            // then
            database.attachmentDao().getById("my-attachment-id-1")?.beat shouldBe false
            database.attachmentDao().getById("my-attachment-id-2")?.let { it.beat to it.updatedAt } shouldBe
                (true to Instant.parse("2026-11-17T10:00:00Z"))
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should remove the audio from its song`() = runTest {
            // given
            coEvery { files.take(any()) } returns AudioFileInfo(title = "my-title", durationMillis = null)
            val id = repository.addFile(songId = "my-song-id", uri = "my-uri").orEmpty()

            // when
            repository.delete(id)

            // then
            repository.observeAttachments("my-song-id").first() shouldBe emptyList()
            coVerifySequence { files.take("my-uri") }
        }
    }
}
