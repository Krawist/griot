package com.griot.feature.editor.audio

import com.griot.core.data.audio.FakeAttachmentRepository
import com.griot.core.data.audio.FakeAudioFiles
import com.griot.core.data.audio.FakeAudioPlayer
import com.griot.core.data.audio.FakeCoverReader
import com.griot.core.data.model.Attachment
import com.griot.core.data.model.AttachmentKind
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class BeatViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class UiState {
        @Test
        fun `should take the newest file of the song that can be read`() = runTest {
            // given
            val attachments = FakeAttachmentRepository(
                attachments = listOf(
                    Attachment(
                        id = "my-attachment-id-1",
                        songId = "my-song-id",
                        kind = AttachmentKind.FILE,
                        uri = "my-uri-1",
                        title = "my-title-1",
                        createdAt = Instant.parse("2026-11-17T10:00:00Z"),
                    ),
                    Attachment(
                        id = "my-attachment-id-2",
                        songId = "my-song-id",
                        kind = AttachmentKind.FILE,
                        uri = "my-uri-2",
                        title = "my-title-2",
                        durationMillis = 200_000,
                        createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                    ),
                ),
            )
            val viewModel = BeatViewModel(
                songId = "my-song-id",
                attachments = attachments,
                files = FakeAudioFiles(missing = setOf("my-uri-1")),
                covers = FakeCoverReader(),
                player = FakeAudioPlayer(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val beat = viewModel.uiState.first { it?.cover != null }

            // then
            beat?.let { Triple(it.title, it.uri, it.durationMillis) } shouldBe
                Triple("my-title-2", "my-uri-2", 200_000L)
        }

        @Test
        fun `should have no beat without a file`() = runTest {
            // given
            val viewModel = BeatViewModel(
                songId = "my-song-id",
                attachments = FakeAttachmentRepository(),
                files = FakeAudioFiles(),
                covers = FakeCoverReader(),
                player = FakeAudioPlayer(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val beat = viewModel.uiState.value

            // then
            beat.shouldBeNull()
        }
    }

    @Nested
    inner class OnPlayToggle {
        @Test
        fun `should play the beat, then pause it`() = runTest {
            // given
            val player = FakeAudioPlayer()
            val viewModel = BeatViewModel(
                songId = "my-song-id",
                attachments = FakeAttachmentRepository(
                    attachments = listOf(
                        Attachment(
                            id = "my-attachment-id",
                            songId = "my-song-id",
                            kind = AttachmentKind.FILE,
                            uri = "my-uri",
                            title = "my-title",
                            createdAt = Instant.parse("2026-11-17T10:00:00Z"),
                        ),
                    ),
                ),
                files = FakeAudioFiles(),
                covers = FakeCoverReader(),
                player = player,
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { it != null }

            // when
            viewModel.onPlayToggle()
            viewModel.uiState.first { it?.playing == true }
            viewModel.onPlayToggle()
            viewModel.onRewind()

            // then
            player.calls shouldContainExactly listOf("play my-uri my-title", "pause", "rewind 15000")
        }
    }
}
