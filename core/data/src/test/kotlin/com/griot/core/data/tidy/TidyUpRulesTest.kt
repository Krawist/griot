package com.griot.core.data.tidy

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import java.time.Instant
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class TidyUpRulesTest {
    @Nested
    inner class FindDuplicates {
        @Test
        fun `should group the songs with the same text, whatever the case, accents, punctuation and order`() {
            // given
            val songs = listOf(
                "Je marche seul\nLes néons pleurent" to "my-song-id-1",
                "les neons pleurent !\nje  MARCHE seul" to "my-song-id-2",
                "Une autre chanson" to "my-song-id-3",
            ).map { (lyrics, id) ->
                LibrarySong(
                    id = id,
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.IN_PROGRESS,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = lyrics,
                )
            }

            // when
            val duplicates = findDuplicates(songs)

            // then
            duplicates.map { group -> group.map { it.id } } shouldContainExactly
                listOf(listOf("my-song-id-1", "my-song-id-2"))
        }

        @Test
        fun `should never group songs without text`() {
            // given
            val songs = listOf("my-song-id-1", "my-song-id-2").map {
                LibrarySong(
                    id = it,
                    title = "",
                    language = SongLanguage.FRENCH,
                    status = SongStatus.DRAFT,
                    updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = " ",
                )
            }

            // when
            val duplicates = findDuplicates(songs)

            // then
            duplicates.shouldBeEmpty()
        }
    }

    @Nested
    inner class StaleDrafts {
        @Test
        fun `should keep the drafts untouched for more than 30 days and not archived yet`() {
            // given
            val songs = listOf(
                Triple("my-song-id-1", SongStatus.DRAFT, emptyList()),
                Triple("my-song-id-2", SongStatus.IN_PROGRESS, emptyList()),
                Triple("my-song-id-3", SongStatus.DRAFT, listOf("archives")),
            ).map { (id, status, folders) ->
                LibrarySong(
                    id = id,
                    title = "my-title",
                    language = SongLanguage.FRENCH,
                    status = status,
                    folderIds = folders,
                    updatedAt = Instant.parse("2026-10-01T10:00:00Z"),
                    sectionCount = 1,
                    firstVerse = null,
                    lyrics = "",
                )
            }

            // when
            val stale = staleDrafts(songs, now = Instant.parse("2026-11-16T10:00:00Z"))

            // then
            stale.map { it.id } shouldContainExactly listOf("my-song-id-1")
        }

        @Test
        fun `should leave a draft changed less than 30 days ago`() {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                updatedAt = Instant.parse("2026-10-20T10:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "",
            )

            // when
            val stale = staleDrafts(listOf(song), now = Instant.parse("2026-11-16T10:00:00Z"))

            // then
            stale.shouldBeEmpty()
        }
    }
}
