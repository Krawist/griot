package com.griot.feature.editor.audio

import com.griot.core.data.audio.FakeAttachmentRepository
import com.griot.core.data.audio.FakeVoiceRecorder
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.data.song.FakeSongEditorRepository
import com.griot.core.data.song.SectionLabel
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RecordingViewModelTest {
    private val document = SongDocument(
        id = "my-song-id",
        title = "my-title",
        language = SongLanguage.FRENCH,
        sections = listOf(
            SongSection(
                id = "my-section-id",
                kind = SectionKind.CHORUS,
                name = null,
                repeatOf = null,
                verses = listOf(
                    SongVerse(id = "my-verse-id-1", text = "a"),
                    SongVerse(id = "my-verse-id-2", text = "b"),
                ),
            ),
        ),
    )

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
        fun `should start recording, attached to the verse of a memo on a verse`() = runTest {
            // given
            val recorder = FakeVoiceRecorder()

            // when
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id-2",
                onVerse = true,
                editor = FakeSongEditorRepository(document = document),
                recorder = recorder,
                attachments = FakeAttachmentRepository(),
            )

            // then
            val label = SectionLabel(kind = SectionKind.CHORUS, number = null, name = null)
            viewModel.uiState.first { it.targets.isNotEmpty() }.let { it.targets to it.target } shouldBe (
                listOf(
                    MemoTarget(sectionId = "my-section-id", verseId = "my-verse-id-2", label = label, verseNumber = 2),
                    MemoTarget(sectionId = "my-section-id", label = label),
                    MemoTarget(),
                ) to MemoTarget(sectionId = "my-section-id", verseId = "my-verse-id-2", label = label, verseNumber = 2)
                )
            recorder.calls shouldContainExactly listOf("start")
        }

        @Test
        fun `should attach a memo from the top bar to the section of the verse selected`() = runTest {
            // when
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id-1",
                onVerse = false,
                editor = FakeSongEditorRepository(document = document),
                recorder = FakeVoiceRecorder(),
                attachments = FakeAttachmentRepository(),
            )

            // then
            viewModel.uiState.first { it.targets.isNotEmpty() }.target.verseId shouldBe null
        }

        @Test
        fun `should say when the microphone cannot be used`() = runTest {
            // when
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = null,
                onVerse = false,
                editor = FakeSongEditorRepository(document = document),
                recorder = FakeVoiceRecorder(available = false),
                attachments = FakeAttachmentRepository(),
            )

            // then
            viewModel.uiState.first { it.failed }.failed shouldBe true
        }
    }

    @Nested
    inner class OnStop {
        @Test
        fun `should keep the memo, attached to the target chosen`() = runTest {
            // given
            val attachments = FakeAttachmentRepository()
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = null,
                onVerse = false,
                editor = FakeSongEditorRepository(document = document),
                recorder = FakeVoiceRecorder(),
                attachments = attachments,
            )
            val section = viewModel.uiState.first { it.targets.isNotEmpty() }.targets.first()
            viewModel.onTarget(section)
            var done = false

            // when
            viewModel.onStop(title = "my-title", onDone = { done = true })

            // then
            done shouldBe true
            attachments.calls shouldContainExactly listOf("memo my-song-id my-uri 1000 my-title my-section-id null")
        }
    }

    @Nested
    inner class OnCancel {
        @Test
        fun `should throw the memo away`() = runTest {
            // given
            val recorder = FakeVoiceRecorder()
            val attachments = FakeAttachmentRepository()
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = null,
                onVerse = false,
                editor = FakeSongEditorRepository(document = document),
                recorder = recorder,
                attachments = attachments,
            )

            // when
            viewModel.onCancel()

            // then
            recorder.calls shouldContainExactly listOf("start", "cancel")
            attachments.calls shouldBe emptyList()
        }
    }

    @Nested
    inner class OnPauseToggle {
        @Test
        fun `should pause, then resume`() = runTest {
            // given
            val recorder = FakeVoiceRecorder()
            val viewModel = RecordingViewModel(
                songId = "my-song-id",
                verseId = null,
                onVerse = false,
                editor = FakeSongEditorRepository(document = document),
                recorder = recorder,
                attachments = FakeAttachmentRepository(),
            )

            // when
            viewModel.onPauseToggle()
            viewModel.onPauseToggle()

            // then
            recorder.calls shouldContainExactly listOf("start", "pause", "resume")
        }
    }
}
