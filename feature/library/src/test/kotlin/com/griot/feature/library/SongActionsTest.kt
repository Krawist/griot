package com.griot.feature.library

import com.griot.core.data.export.FakeMarkdownExporter
import com.griot.core.data.model.ExportFile
import com.griot.core.data.organisation.FakeFolderRepository
import com.griot.core.data.song.FakeSongRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SongActionsTest {
    @Nested
    inner class OnArchive {
        @Test
        fun `should file the song in Archives`() = runTest(UnconfinedTestDispatcher()) {
            // given
            val folders = FakeFolderRepository()
            val actions = SongActions(
                songs = FakeSongRepository(),
                folders = folders,
                exporter = FakeMarkdownExporter(),
                scope = this,
            )

            // when
            actions.onArchive(id = "my-song-id", archived = true)

            // then
            folders.calls shouldContainExactly listOf("file my-song-id +archives -")
        }

        @Test
        fun `should take the song out of Archives`() = runTest(UnconfinedTestDispatcher()) {
            // given
            val folders = FakeFolderRepository()
            val actions = SongActions(
                songs = FakeSongRepository(),
                folders = folders,
                exporter = FakeMarkdownExporter(),
                scope = this,
            )

            // when
            actions.onArchive(id = "my-song-id", archived = false)

            // then
            folders.calls shouldContainExactly listOf("file my-song-id + -archives")
        }
    }

    @Nested
    inner class OnExport {
        @Test
        fun `should export the song to share it at once`() = runTest(UnconfinedTestDispatcher()) {
            // given
            val exporter = FakeMarkdownExporter()
            val actions = SongActions(
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                exporter = exporter,
                scope = this,
            )

            // when
            actions.onExport(id = "my-song-id", share = true)

            // then
            actions.exported.value shouldBe SongExport(
                file = ExportFile(name = "my-name.md", mimeType = "text/markdown", path = "my-path"),
                share = true,
            )
            exporter.calls shouldContainExactly listOf("song my-song-id")
        }

        @Test
        fun `should export nothing for a song gone`() = runTest(UnconfinedTestDispatcher()) {
            // given
            val actions = SongActions(
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                exporter = FakeMarkdownExporter(file = null),
                scope = this,
            )

            // when
            actions.onExport(id = "my-song-id", share = false)

            // then
            actions.exported.value.shouldBeNull()
        }
    }

    @Nested
    inner class OnSaveExport {
        @Test
        fun `should save the song exported in the document picked`() = runTest(UnconfinedTestDispatcher()) {
            // given
            val exporter = FakeMarkdownExporter()
            val actions = SongActions(
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                exporter = exporter,
                scope = this,
            )
            actions.onExport(id = "my-song-id", share = false)

            // when
            actions.onSaveExport("my-uri")

            // then
            exporter.calls shouldContainExactly listOf("song my-song-id", "save my-name.md my-uri")
        }
    }
}
