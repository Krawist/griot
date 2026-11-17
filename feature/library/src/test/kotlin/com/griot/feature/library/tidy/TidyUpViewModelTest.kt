package com.griot.feature.library.tidy

import com.griot.core.ai.AiLocation
import com.griot.core.ai.Classification
import com.griot.core.ai.FakeAiAssistant
import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.TidyUpCandidates
import com.griot.core.data.model.TidyUpKind
import com.griot.core.data.organisation.FakeFolderRepository
import com.griot.core.data.organisation.FakeGenreRepository
import com.griot.core.data.song.FakeSongRepository
import com.griot.core.data.tidy.FakeTidyUpRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Instant
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
internal class TidyUpViewModelTest {
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
        fun `should propose the duplicates and the old drafts without a model, and mark the proposals looked at`() =
            runTest {
                // given
                val songs = listOf("my-song-id-1", "my-song-id-2", "my-song-id-3").map {
                    LibrarySong(
                        id = it,
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        updatedAt = Instant.parse("2026-09-01T10:00:00Z"),
                        sectionCount = 1,
                        firstVerse = null,
                        lyrics = "",
                    )
                }
                val tidy = FakeTidyUpRepository(
                    candidates = TidyUpCandidates(
                        duplicates = listOf(songs.take(2)),
                        staleDrafts = listOf(songs[2]),
                        untitled = listOf(songs[0]),
                    ),
                )
                val viewModel = TidyUpViewModel(
                    tidy = tidy,
                    songs = FakeSongRepository(),
                    folders = FakeFolderRepository(),
                    genres = FakeGenreRepository(),
                    assistant = FakeAiAssistant(),
                )

                // when
                val state = viewModel.uiState.first { !it.loading }

                // then
                state.proposals.map { it.kind to it.songIds } shouldContainExactly listOf(
                    TidyUpKind.DUPLICATE to listOf("my-song-id-1", "my-song-id-2"),
                    TidyUpKind.ARCHIVE to listOf("my-song-id-3"),
                )
                tidy.calls shouldContainExactly listOf("reviewed")
            }

        @Test
        fun `should add the titles and classifications of the AI`() = runTest {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "",
                language = SongLanguage.FRENCH,
                status = SongStatus.IN_PROGRESS,
                updatedAt = Instant.parse("2026-11-16T10:00:00Z"),
                sectionCount = 1,
                firstVerse = "my-verse",
                lyrics = "my-verse",
            )
            val folder =
                Folder(
                    id = "my-folder-id",
                    name = "EP",
                    color = FolderColor.MAGENTA,
                    kind = FolderKind.CUSTOM,
                    songCount = 0,
                )
            val afro = Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)
            val viewModel = TidyUpViewModel(
                tidy = FakeTidyUpRepository(
                    candidates = TidyUpCandidates(untitled = listOf(song), unclassified = listOf(song)),
                ),
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(folders = listOf(folder)),
                genres = FakeGenreRepository(genres = listOf(afro)),
                assistant = FakeAiAssistant(
                    location = AiLocation.DEVICE,
                    title = "my-title",
                    classification = Classification(folder = "EP", genre = "Afro"),
                ),
            )

            // when
            val state = viewModel.uiState.first { !it.loading && !it.thinking }

            // then
            state.proposals shouldContainExactly listOf(
                TidyUpProposal(
                    id = "TITLE:my-song-id",
                    kind = TidyUpKind.TITLE,
                    songIds = listOf("my-song-id"),
                    songTitle = "",
                    firstVerse = "my-verse",
                    title = "my-title",
                ),
                TidyUpProposal(
                    id = "CLASSIFY:my-song-id",
                    kind = TidyUpKind.CLASSIFY,
                    songIds = listOf("my-song-id"),
                    songTitle = "",
                    folder = folder,
                    genre = afro,
                ),
            )
        }
    }

    @Nested
    inner class OnAccept {
        @Test
        fun `should merge the duplicates into the first song and archive the old drafts`() = runTest {
            // given
            val songs = listOf("my-song-id-1", "my-song-id-2").map {
                LibrarySong(
                    id = it,
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    updatedAt = Instant.parse("2026-09-01T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }
            val tidy = FakeTidyUpRepository(
                candidates = TidyUpCandidates(duplicates = listOf(songs), staleDrafts = listOf(songs[0])),
            )
            val folders = FakeFolderRepository()
            val viewModel = TidyUpViewModel(
                tidy = tidy,
                songs = FakeSongRepository(),
                folders = folders,
                genres = FakeGenreRepository(),
                assistant = FakeAiAssistant(),
            )
            viewModel.uiState.first { !it.loading }

            // when
            viewModel.onAcceptAll()

            // then
            viewModel.uiState.value.proposals.map { it.decision }.distinct() shouldContainExactly
                listOf(TidyUpDecision.ACCEPTED)
            tidy.calls shouldContainExactly listOf("reviewed", "merge my-song-id-1 my-song-id-2")
            folders.calls shouldContainExactly listOf("file my-song-id-1 +archives -")
        }
    }

    @Nested
    inner class OnRefuse {
        @Test
        fun `should never propose the duplicates again`() = runTest {
            // given
            val songs = listOf("my-song-id-2", "my-song-id-1").map {
                LibrarySong(
                    id = it,
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }
            val tidy = FakeTidyUpRepository(candidates = TidyUpCandidates(duplicates = listOf(songs)))
            val viewModel = TidyUpViewModel(
                tidy = tidy,
                songs = FakeSongRepository(),
                folders = FakeFolderRepository(),
                genres = FakeGenreRepository(),
                assistant = FakeAiAssistant(),
            )
            val id = viewModel.uiState.first { !it.loading }.proposals.single().id

            // when
            viewModel.onRefuse(id)

            // then
            viewModel.uiState.value.proposals.single().decision shouldBe TidyUpDecision.REFUSED
            tidy.calls shouldContainExactly listOf("reviewed", "refuse DUPLICATE my-song-id-1,my-song-id-2")
        }
    }
}
