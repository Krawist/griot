package com.griot.core.data.importing

import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SectionKind
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.SongRepository
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TextImporterImplTest {
    private val songs = mockk<SongRepository>()
    private val preferences = FakeUserPreferencesRepository()
    private val importer = TextImporterImpl(songs = songs, preferences = preferences)

    @Nested
    inner class Import {
        @Test
        fun `should create a song per text, split into sections and named after its file`() = runTest {
            // given
            coEvery { songs.importSong(any(), any()) } returns "my-song-id"

            // when
            val result = importer.import(listOf(ImportedText(name = "my-name", text = "[Couplet]\na\n\n[Refrain]\nb")))

            // then
            result shouldBe ImportResult(songs = 1, sections = 2, failed = emptyList())
            coVerifySequence {
                songs.importSong(
                    title = "my-name",
                    sections = listOf(
                        NewSection(kind = SectionKind.VERSE, name = null, lines = listOf("a"), repeatOf = null),
                        NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("b"), repeatOf = null),
                    ),
                )
            }
        }

        @Test
        fun `should report an empty or unreadable file by name and go on`() = runTest {
            // given
            coEvery { songs.importSong(any(), any()) } returns "my-song-id"

            // when
            val result = importer.import(
                listOf(
                    ImportedText(name = "my-name-1", text = " \n "),
                    ImportedText(name = "my-name-2", text = null),
                    ImportedText(name = "my-name-3", text = "a"),
                ),
            )

            // then
            result shouldBe ImportResult(songs = 1, sections = 1, failed = listOf("my-name-1", "my-name-2"))
            coVerifySequence {
                songs.importSong(
                    title = "my-name-3",
                    sections = listOf(
                        NewSection(kind = SectionKind.VERSE, name = null, lines = listOf("a"), repeatOf = null),
                    ),
                )
            }
        }

        @Test
        fun `should leave out the title line of a note, already the song's name`() = runTest {
            // given
            coEvery { songs.importSong(any(), any()) } returns "my-song-id"

            // when
            val result = importer.import(
                listOf(ImportedText(name = "my-name", text = "My-name\n\n[Refrain]\na\n\n[Refrain]")),
            )

            // then
            result shouldBe ImportResult(songs = 1, sections = 2, failed = emptyList())
            coVerifySequence {
                songs.importSong(
                    title = "my-name",
                    sections = listOf(
                        NewSection(kind = SectionKind.CHORUS, name = null, lines = listOf("a"), repeatOf = null),
                        NewSection(kind = SectionKind.CHORUS, name = null, lines = emptyList(), repeatOf = 0),
                    ),
                )
            }
        }

        @Test
        fun `should create nothing without text`() = runTest {
            // when
            val result = importer.import(emptyList())

            // then
            result shouldBe ImportResult(songs = 0, sections = 0, failed = emptyList())
            verify { songs wasNot Called }
        }

        @Test
        fun `should ask for a tidy-up once songs are imported`() = runTest {
            // given
            coEvery { songs.importSong(any(), any()) } returns "my-song-id"

            // when
            importer.import(listOf(ImportedText(name = "my-name", text = "a")))

            // then
            preferences.tidyUpRequested.value shouldBe true
        }

        @Test
        fun `should not ask for a tidy-up when nothing was imported`() = runTest {
            // when
            importer.import(listOf(ImportedText(name = "my-name", text = null)))

            // then
            preferences.tidyUpRequested.value shouldBe false
        }
    }
}
