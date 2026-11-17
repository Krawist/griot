package com.griot.feature.editor

import com.griot.core.data.language.FakeSongLanguageDetector
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.data.song.FakeSongEditorRepository
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SongLanguageViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class OnApply {
        @Test
        fun `should set the language chosen by hand`() = runTest {
            // given
            val editor = FakeSongEditorRepository()
            val viewModel = SongLanguageViewModel(
                songId = "my-song-id",
                editor = editor,
                detector = FakeSongLanguageDetector(),
            )

            // when
            viewModel.onApply(LanguageChoice.ENGLISH)

            // then
            editor.calls shouldContainExactly listOf("language my-song-id ENGLISH")
        }

        @Test
        fun `should detect the language at once when the automatic mode is chosen`() = runTest {
            // given
            val detector = FakeSongLanguageDetector(language = SongLanguage.ENGLISH)
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    languageDetected = false,
                    sections = listOf(
                        SongSection(
                            id = "my-section-id",
                            kind = SectionKind.VERSE,
                            name = null,
                            repeatOf = null,
                            verses = listOf(
                                SongVerse(id = "my-verse-id-1", text = "my-text-1"),
                                SongVerse(id = "my-verse-id-2", text = "my-text-2"),
                            ),
                        ),
                    ),
                ),
            )
            val viewModel = SongLanguageViewModel(
                songId = "my-song-id",
                editor = editor,
                detector = detector,
            )

            // when
            viewModel.onApply(LanguageChoice.AUTOMATIC)

            // then
            editor.calls shouldContainExactly listOf("detected language my-song-id ENGLISH")
            detector.texts shouldContainExactly listOf("my-text-1\nmy-text-2")
        }

        @Test
        fun `should keep the language when the automatic mode is chosen for lyrics too short to tell`() = runTest {
            // given
            val detector = FakeSongLanguageDetector(language = null)
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.ENGLISH,
                    languageDetected = false,
                    sections = listOf(
                        SongSection(
                            id = "my-section-id",
                            kind = SectionKind.VERSE,
                            name = null,
                            repeatOf = null,
                            verses = listOf(
                                SongVerse(id = "my-verse-id-1", text = "my-text-1"),
                                SongVerse(id = "my-verse-id-2", text = "my-text-2"),
                            ),
                        ),
                    ),
                ),
            )
            val viewModel = SongLanguageViewModel(
                songId = "my-song-id",
                editor = editor,
                detector = detector,
            )

            // when
            viewModel.onApply(LanguageChoice.AUTOMATIC)

            // then
            editor.calls shouldContainExactly listOf("detected language my-song-id ENGLISH")
        }
    }
}
