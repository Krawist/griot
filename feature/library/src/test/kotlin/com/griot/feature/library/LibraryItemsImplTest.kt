package com.griot.feature.library

import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
import com.griot.core.design.format.RelativeTime
import com.griot.core.phonetics.PhoneticEngines
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class LibraryItemsImplTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-16T16:00:00Z"), ZoneOffset.UTC)
    private val items = LibraryItemsImpl(engines = PhoneticEngines(), clock = clock)

    @Nested
    inner class ItemOf {
        @Test
        fun `should give the card its folder, its genre and when it last changed`() {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.DRAFT,
                folderId = "my-folder-id-2",
                genreId = "afro",
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = null,
                lyrics = "",
            )
            val folders = listOf("my-folder-id-1", "my-folder-id-2").map {
                Folder(id = it, name = "my-name", color = FolderColor.MAGENTA, kind = FolderKind.CUSTOM, songCount = 1)
            }
            val genre = Genre(id = "afro", name = "", builtIn = BuiltInGenre.AFRO)

            // when
            val item = items.itemOf(song = song, folders = folders, genres = listOf(genre), now = clock.instant())

            // then
            item shouldBe LibraryItem(
                song = song,
                updated = RelativeTime.HoursAgo(2),
                folder = folders[1],
                genre = genre,
            )
        }

        @Test
        fun `should give the card the rhymes of its first section`() {
            // given
            val song = LibrarySong(
                id = "my-song-id",
                title = "my-title",
                language = SongLanguage.FRENCH,
                status = SongStatus.IN_PROGRESS,
                updatedAt = Instant.parse("2026-11-16T14:00:00Z"),
                sectionCount = 1,
                firstVerse = "Je marche seul dans la ville",
                lyrics = "",
                firstSection = listOf(
                    "Je marche seul dans la ville",
                    "Les néons pleurent sur le bitume",
                    "",
                    "Mon cœur bat, fragile",
                    "Comme une flamme dans la brume",
                ),
            )

            // when
            val item = items.itemOf(song = song, folders = emptyList(), genres = emptyList(), now = clock.instant())

            // then
            item.rhymes shouldContainExactly listOf(0, 1, 0, 1)
        }
    }
}
