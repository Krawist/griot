package com.griot.feature.editor.ai

import com.griot.core.ai.AiLocation
import com.griot.core.ai.CheckedSuggestion
import com.griot.core.ai.FakeAiAssistant
import com.griot.core.ai.VerseRequest
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import com.griot.core.data.song.FakeSongEditorRepository
import com.griot.core.data.song.SectionLabel
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.collections.shouldContain
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
internal class AiPanelViewModelTest {
    private val engines = PhoneticEngines()

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
        fun `should show the verse with its place, its count and its rhyming ending`() = runTest {
            // given
            val viewModel = AiPanelViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id-2",
                language = SongLanguage.FRENCH,
                editor = FakeSongEditorRepository(
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
                                    SongVerse(id = "my-verse-id-1", text = "La poussière danse sur nos vies."),
                                    SongVerse(id = "my-verse-id-2", text = "Le soleil cogne sur la tôle"),
                                ),
                            ),
                        ),
                    ),
                ),
                engines = engines,
                assistant = FakeAiAssistant(),
            )

            // when
            val uiState = viewModel.uiState.first { !it.loading }

            // then
            uiState.verse shouldBe "Le soleil cogne sur la tôle"
            uiState.section shouldBe SectionLabel(kind = SectionKind.VERSE, number = 1, name = null)
            uiState.number shouldBe 2
            uiState.syllables shouldBe 8
            uiState.ending shouldBe "ôle"
        }

        @Test
        fun `should cross out the rhymes the song already has`() = runTest {
            // given
            val viewModel = AiPanelViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id-1",
                language = SongLanguage.FRENCH,
                editor = FakeSongEditorRepository(
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
                                    SongVerse(id = "my-verse-id-1", text = "Le soleil cogne sur la tôle"),
                                    SongVerse(id = "my-verse-id-2", text = "Chacun joue son rôle"),
                                ),
                            ),
                        ),
                    ),
                ),
                engines = engines,
                assistant = FakeAiAssistant(),
            )

            // when
            val uiState = viewModel.uiState.first { !it.loading }

            // then
            uiState.rhymes shouldContain RhymeChip(word = "rôle", syllables = 1, used = true)
            uiState.rhymes shouldContain RhymeChip(word = "drôle", syllables = 1, used = false)
        }
    }

    @Nested
    inner class OnTabSelected {
        @Test
        fun `should ask the model for versions of the verse with its section and neighbours`() = runTest {
            // given
            val assistant = FakeAiAssistant(
                location = AiLocation.DEVICE,
                suggestions = listOf(CheckedSuggestion(text = "my-suggestion", syllables = 8, guessed = false)),
            )
            val viewModel = AiPanelViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id-2",
                language = SongLanguage.FRENCH,
                editor = FakeSongEditorRepository(
                    document = SongDocument(
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
                                    SongVerse(id = "my-verse-id-1", text = "La poussière danse sur nos vies."),
                                    SongVerse(id = "my-verse-id-2", text = "Le soleil cogne sur la tôle"),
                                ),
                            ),
                        ),
                    ),
                ),
                engines = engines,
                assistant = assistant,
            )
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onTabSelected(AiTab.REPHRASE)

            // then
            viewModel.uiState.first { it.rephrase.answered }.rephrase shouldBe AiContent(
                items = listOf(CheckedSuggestion(text = "my-suggestion", syllables = 8, guessed = false)),
                answered = true,
            )
            assistant.requests shouldContainExactly listOf(
                "rephrase" to VerseRequest(
                    language = PhoneticLanguage.FRENCH,
                    section = "chorus",
                    verse = "Le soleil cogne sur la tôle",
                    before = listOf("La poussière danse sur nos vies."),
                    after = emptyList(),
                    targetSyllables = 8,
                ),
            )
        }

        @Test
        fun `should say when no model is available`() = runTest {
            // given
            val viewModel = AiPanelViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id",
                language = SongLanguage.FRENCH,
                editor = FakeSongEditorRepository(
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
                                verses = listOf(SongVerse(id = "my-verse-id", text = "Le soleil cogne sur la tôle")),
                            ),
                        ),
                    ),
                ),
                engines = engines,
                assistant = FakeAiAssistant(location = null),
            )
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onTabSelected(AiTab.CRITIQUE)

            // then
            viewModel.uiState.first { it.critique.answered }.critique shouldBe
                AiContent(answered = true, unavailable = true)
        }
    }

    @Nested
    inner class OnMoreIdeas {
        @Test
        fun `should ask the model again for the tab shown`() = runTest {
            // given
            val assistant = FakeAiAssistant(location = AiLocation.ONLINE, remarks = listOf("my-remark"))
            val viewModel = AiPanelViewModel(
                songId = "my-song-id",
                verseId = "my-verse-id",
                language = SongLanguage.FRENCH,
                editor = FakeSongEditorRepository(
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
                                verses = listOf(SongVerse(id = "my-verse-id", text = "Le soleil cogne sur la tôle")),
                            ),
                        ),
                    ),
                ),
                engines = engines,
                assistant = assistant,
            )
            viewModel.uiState.first { !it.loading }
            viewModel.onTabSelected(AiTab.CRITIQUE)
            viewModel.uiState.first { it.critique.answered }

            // when
            viewModel.onMoreIdeas()

            // then
            viewModel.uiState.first { it.location == AiLocation.ONLINE }.critique.items shouldContainExactly
                listOf("my-remark")
            assistant.requests.map { it.first } shouldContainExactly listOf("critique", "critique")
        }
    }
}
