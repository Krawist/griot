package com.griot.core.data.idea

import android.content.ContextWrapper
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.language.FakeSongLanguageDetector
import com.griot.core.data.model.Idea
import com.griot.core.data.preferences.FakeUserPreferencesRepository
import com.griot.core.data.song.SongEditorRepositoryImpl
import com.griot.core.data.song.SongRepositoryImpl
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database with the real song repository: the ideas are checked as they are read back.
internal class IdeaRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC)
    private val songs = SongRepositoryImpl(
        songDao = database.songDao(),
        sectionDao = database.sectionDao(),
        verseDao = database.verseDao(),
        songGenreDao = database.songGenreDao(),
        favoriteDao = database.favoriteDao(),
        preferences = FakeUserPreferencesRepository(),
        detector = FakeSongLanguageDetector(),
        clock = clock,
    )
    private val repository = IdeaRepositoryImpl(
        // Text ideas never touch the context: only images are copied with it.
        context = ContextWrapper(null),
        ideaDao = database.ideaDao(),
        songs = songs,
        clock = clock,
    )

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class AddIdea {
        @Test
        fun `should save a free idea, without the spaces around it`() = runTest {
            // when
            val id = repository.addIdea(" my-text ")

            // then
            repository.observeIdeas().first() shouldContainExactly listOf(
                Idea(
                    id = id,
                    text = "my-text",
                    imagePath = null,
                    songId = null,
                    songTitle = null,
                    createdAt = Instant.parse("2026-11-16T10:00:00Z"),
                ),
            )
        }
    }

    @Nested
    inner class LinkIdea {
        @Test
        fun `should show the idea with the title of its song`() = runTest {
            // given
            val songId = songs.createSong()
            songs.renameSong(id = songId, title = "my-title")
            val id = repository.addIdea("my-text")

            // when
            repository.linkIdea(id = id, songId = songId)

            // then
            repository.observeSongIdeas(songId).first().single().let { it.songId to it.songTitle } shouldBe
                (songId to "my-title")
        }
    }

    @Nested
    inner class StartSong {
        @Test
        fun `should start a song whose first verse is the idea, linked to it`() = runTest {
            // given
            val id = repository.addIdea("my-text")

            // when
            val songId = repository.startSong(id)

            // then
            SongEditorRepositoryImpl(
                database = database,
                songDao = database.songDao(),
                sectionDao = database.sectionDao(),
                verseDao = database.verseDao(),
                clock = clock,
            ).observeDocument(songId).first()?.sections?.single()?.verses?.map { it.text } shouldBe listOf("my-text")
            repository.observeIdeas().first().single().songId shouldBe songId
        }
    }

    @Nested
    inner class DeleteIdea {
        @Test
        fun `should hide a deleted idea until it is restored`() = runTest {
            // given
            val id = repository.addIdea("my-text")
            repository.deleteIdea(id)
            repository.observeIdeas().first().shouldBeEmpty()

            // when
            repository.restoreIdea(id)

            // then
            repository.observeIdeas().first().map { it.id } shouldContainExactly listOf(id)
        }
    }
}
