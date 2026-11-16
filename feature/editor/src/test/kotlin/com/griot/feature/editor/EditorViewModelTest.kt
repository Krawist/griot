package com.griot.feature.editor

import com.griot.core.data.model.PastedSections
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.FakeSongEditorRepository
import com.griot.core.data.song.FakeSongRepository
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@OptIn(ExperimentalCoroutinesApi::class)
internal class EditorViewModelTest {
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
        fun `should show the sections with their labels and places`() = runTest {
            // given
            val verse = SongSection(
                id = "my-section-id-1",
                kind = SectionKind.VERSE,
                name = null,
                repeatOf = null,
                verses = listOf(SongVerse(id = "my-verse-id", text = "my-text")),
            )
            val chorus = SongSection(
                id = "my-section-id-2",
                kind = SectionKind.CHORUS,
                name = null,
                repeatOf = null,
                verses = emptyList(),
            )
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = FakeSongEditorRepository(
                    document = SongDocument(
                        id = "my-song-id",
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        sections = listOf(verse, chorus),
                    ),
                ),
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state shouldBe EditorUiState(
                loading = false,
                title = "my-title",
                language = SongLanguage.FRENCH,
                sections = listOf(
                    EditorSection(
                        section = verse,
                        label = SectionLabel(kind = SectionKind.VERSE, number = 1, name = null),
                        first = true,
                        last = false,
                    ),
                    EditorSection(
                        section = chorus,
                        label = SectionLabel(kind = SectionKind.CHORUS, number = null, name = null),
                        first = false,
                        last = true,
                    ),
                ),
            )
        }

        @Test
        fun `should tell a song that no longer exists`() = runTest {
            // given
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = FakeSongEditorRepository(document = null),
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state shouldBe EditorUiState(loading = false, missing = true)
        }
    }

    @Nested
    inner class OnTitleChange {
        @Test
        fun `should rename the song`() = runTest {
            // given
            val songs = FakeSongRepository()
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = FakeSongEditorRepository(),
                    songs = songs,
                    preferences = FakeUserPreferencesRepository(),
                )

            // when
            viewModel.onTitleChange("my-title")

            // then
            songs.calls shouldContainExactly listOf("rename my-song-id my-title")
        }
    }

    @Nested
    inner class OnSectionChanges {
        @Test
        fun `should add a section of the kind picked`() = runTest {
            // given
            val editor = FakeSongEditorRepository()
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )

            // when
            viewModel.onAddSection(SectionKind.BRIDGE)

            // then
            editor.calls shouldContainExactly listOf("add my-song-id BRIDGE")
        }

        @Test
        fun `should repeat, rename, move and delete the section`() = runTest {
            // given
            val editor = FakeSongEditorRepository()
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )

            // when
            viewModel.onRepeatSection("my-section-id")
            viewModel.onRenameSection(sectionId = "my-section-id", name = "my-name")
            viewModel.onMoveSection(sectionId = "my-section-id", offset = 1)
            viewModel.onDeleteSection("my-section-id")

            // then
            editor.calls shouldContainExactly listOf(
                "repeat my-section-id",
                "rename my-section-id my-name",
                "move my-section-id 1",
                "delete my-section-id",
            )
        }
    }

    @Nested
    inner class OnVerseChange {
        @Test
        fun `should save the verse on one line`() = runTest {
            // given
            val editor = FakeSongEditorRepository()
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )

            // when
            viewModel.onVerseChange(verseId = "my-verse-id", text = "my-text\nmy-other-text")

            // then
            editor.calls shouldContainExactly listOf("verse my-verse-id my-text my-other-text")
        }
    }

    @Nested
    inner class OnVerseLines {
        @Test
        fun `should split the verse and focus the new one where the cursor was`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
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
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseLines(verseId = "my-verse-id-1", lines = listOf("my-line-1", "my-line-2"), cursor = 0)

            // then
            viewModel.uiState.first { it.focus != null }.focus shouldBe
                VerseFocus(verseId = "split-verse-id-1", text = "my-line-2", cursor = 0)
            editor.calls shouldContainExactly listOf("split my-verse-id-1 my-line-1|my-line-2")
        }

        @Test
        fun `should close the section on Enter in an empty last verse`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    sections = listOf(
                        SongSection(
                            id = "my-section-id",
                            kind = SectionKind.VERSE,
                            name = null,
                            repeatOf = null,
                            verses = listOf(
                                SongVerse(id = "my-verse-id-1", text = "my-text-1"),
                                SongVerse(id = "my-verse-id-2", text = ""),
                            ),
                        ),
                    ),
                ),
            )
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseLines(verseId = "my-verse-id-2", lines = listOf("", ""), cursor = 0)

            // then
            viewModel.uiState.first { it.focus != null }.focus shouldBe
                VerseFocus(verseId = "closed-verse-id", text = "", cursor = 0)
            editor.calls shouldContainExactly listOf("close my-verse-id-2")
        }

        @Test
        fun `should split an empty verse that is not the last one`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
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
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseLines(verseId = "my-verse-id-1", lines = listOf("", ""), cursor = 0)

            // then
            editor.calls shouldContainExactly listOf("split my-verse-id-1 |")
        }
    }

    @Nested
    inner class OnVerseBackspaceAtStart {
        @Test
        fun `should join the previous verse, the cursor where they meet`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
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
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseBackspaceAtStart(verseId = "my-verse-id-2", text = "my-text-2")

            // then
            viewModel.uiState.first { it.focus != null }.focus shouldBe
                VerseFocus(verseId = "my-verse-id-1", text = "my-text-1my-text-2", cursor = 9)
            editor.calls shouldContainExactly listOf("merge my-verse-id-1 my-verse-id-2")
        }

        @Test
        fun `should leave the first verse of a section as it is`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
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
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseBackspaceAtStart(verseId = "my-verse-id-1", text = "my-text-1")

            // then
            viewModel.uiState.value.focus.shouldBeNull()
            editor.calls.shouldBeEmpty()
        }
    }

    @Nested
    inner class OnFocusHandled {
        @Test
        fun `should forget the verse to focus`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
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
            val viewModel =
                EditorViewModel(
                    songId = "my-song-id",
                    editor = editor,
                    songs = FakeSongRepository(),
                    preferences = FakeUserPreferencesRepository(),
                )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            viewModel.onVerseLines(verseId = "my-verse-id-1", lines = listOf("my-line-1", "my-line-2"), cursor = 0)

            // when
            viewModel.onFocusHandled()

            // then
            viewModel.uiState.first { it.focus == null }.focus.shouldBeNull()
        }
    }

    @Nested
    inner class OnFontSizeChange {
        @ParameterizedTest
        @CsvSource("14, 14", "24, 24", "28, 28", "10, 14", "40, 28")
        fun `should save the size, kept between 14 and 28 sp`(size: Int, saved: Int) = runTest {
            // given
            val preferences = FakeUserPreferencesRepository()
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = FakeSongEditorRepository(),
                songs = FakeSongRepository(),
                preferences = preferences,
            )

            // when
            viewModel.onFontSizeChange(size)

            // then
            preferences.editorFontSize.value shouldBe saved
        }

        @Test
        fun `should show the lyrics at the size saved`() = runTest {
            // given
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = FakeSongEditorRepository(),
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(editorFontSize = 22),
            )
            viewModel.uiState.launchIn(backgroundScope)

            // when
            val state = viewModel.uiState.first { !it.loading }

            // then
            state.fontSize shouldBe 22
        }
    }

    @Nested
    inner class OnPaste {
        @Test
        fun `should create the sections of a pasted text and offer to undo`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    sections = listOf(
                        SongSection(
                            id = "my-section-id",
                            kind = SectionKind.VERSE,
                            name = null,
                            repeatOf = null,
                            verses = listOf(SongVerse(id = "my-verse-id", text = "")),
                        ),
                    ),
                ),
            )
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = editor,
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onVerseLines(verseId = "my-verse-id", lines = listOf("a", "", "[Refrain]", "b"), cursor = 1)

            // then
            viewModel.uiState.first { it.pasteUndo != null }.pasteUndo shouldBe PasteUndo(
                verseId = "my-verse-id",
                lines = listOf("a", "[Refrain]", "b"),
                paste = PastedSections(sectionIds = listOf("pasted-section-id"), verseIds = listOf("pasted-verse-id")),
                sectionCount = 2,
            )
            editor.calls shouldContainExactly listOf("paste my-verse-id 2")
        }

        @Test
        fun `should put the text back into one section on undo`() = runTest {
            // given
            val editor = FakeSongEditorRepository(
                document = SongDocument(
                    id = "my-song-id",
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    sections = listOf(
                        SongSection(
                            id = "my-section-id",
                            kind = SectionKind.VERSE,
                            name = null,
                            repeatOf = null,
                            verses = listOf(SongVerse(id = "my-verse-id", text = "")),
                        ),
                    ),
                ),
            )
            val viewModel = EditorViewModel(
                songId = "my-song-id",
                editor = editor,
                songs = FakeSongRepository(),
                preferences = FakeUserPreferencesRepository(),
            )
            viewModel.uiState.launchIn(backgroundScope)
            viewModel.uiState.first { !it.loading }

            viewModel.onVerseLines(verseId = "my-verse-id", lines = listOf("a", "", "b"), cursor = 1)

            // when
            viewModel.onUndoPaste()

            // then
            viewModel.uiState.first { it.pasteUndo == null }.pasteUndo.shouldBeNull()
            editor.calls shouldContainExactly listOf("paste my-verse-id 2", "undo paste my-verse-id a|b")
        }
    }
}
