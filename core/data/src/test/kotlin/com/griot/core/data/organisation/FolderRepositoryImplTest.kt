package com.griot.core.data.organisation

import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import com.griot.core.data.model.SongLanguage
import com.griot.core.data.model.SongStatus
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

// On an in-memory database: the folders are checked as they are read back.
internal class FolderRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val clock = Clock.fixed(Instant.parse("2026-11-17T10:00:00Z"), ZoneOffset.UTC)
    private val repository = FolderRepositoryImpl(database = database, clock = clock)

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class ObserveFolders {
        @Test
        fun `should list the folders in order, Archives last`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "archives",
                    name = "",
                    color = FolderColor.NEUTRAL,
                    kind = FolderKind.ARCHIVES,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id-1",
                    name = "my-name-1",
                    color = FolderColor.MAGENTA,
                    position = 2,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id-2",
                    name = "my-name-2",
                    color = FolderColor.ORANGE,
                    position = 1,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            val folders = repository.observeFolders().first()

            // then
            folders shouldContainExactly listOf(
                Folder(
                    id = "my-folder-id-2",
                    name = "my-name-2",
                    color = FolderColor.ORANGE,
                    kind = FolderKind.CUSTOM,
                    songCount = 0,
                ),
                Folder(
                    id = "my-folder-id-1",
                    name = "my-name-1",
                    color = FolderColor.MAGENTA,
                    kind = FolderKind.CUSTOM,
                    songCount = 0,
                ),
                Folder(
                    id = "archives",
                    name = "",
                    color = FolderColor.NEUTRAL,
                    kind = FolderKind.ARCHIVES,
                    songCount = 0,
                ),
            )
        }

        @Test
        fun `should count the songs out of the trash in each folder`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id",
                    name = "my-name",
                    color = FolderColor.MAGENTA,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            listOf("my-song-id-1" to false, "my-song-id-2" to true).forEach { (id, deleted) ->
                database.songDao().insert(
                    SongEntity(
                        id = id,
                        title = "my-title",
                        language = SongLanguage.FRENCH,
                        status = SongStatus.DRAFT,
                        createdAt = Instant.parse("2026-11-01T10:00:00Z"),
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                        deleted = deleted,
                    ),
                )
                database.songFolderDao().insert(
                    SongFolderEntity(
                        id = "$id:my-folder-id",
                        songId = id,
                        folderId = "my-folder-id",
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                )
            }

            // when
            val folders = repository.observeFolders().first()

            // then
            folders.single().songCount shouldBe 1
        }
    }

    @Nested
    inner class CreateFolder {
        @Test
        fun `should create the folder after the others`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id",
                    name = "my-name-1",
                    color = FolderColor.MAGENTA,
                    position = 3,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            val id = repository.createFolder(name = " my-name-2 ", color = FolderColor.TURQUOISE)

            // then
            database.folderDao().getById(id) shouldBe FolderEntity(
                id = id,
                name = "my-name-2",
                color = FolderColor.TURQUOISE,
                position = 4,
                updatedAt = Instant.parse("2026-11-17T10:00:00Z"),
            )
        }
    }

    @Nested
    inner class RenameFolder {
        @Test
        fun `should rename a folder of the author`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id",
                    name = "my-name-1",
                    color = FolderColor.MAGENTA,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            repository.renameFolder(id = "my-folder-id", name = "my-name-2")

            // then
            database.folderDao().getById("my-folder-id")?.let { it.name to it.updatedAt } shouldBe
                ("my-name-2" to Instant.parse("2026-11-17T10:00:00Z"))
        }

        @Test
        fun `should keep the name of Archives`() = runTest {
            // given
            val archives = FolderEntity(
                id = "archives",
                name = "",
                color = FolderColor.NEUTRAL,
                kind = FolderKind.ARCHIVES,
                position = 0,
                updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
            )
            database.folderDao().insert(archives)

            // when
            repository.renameFolder(id = "archives", name = "my-name")

            // then
            database.folderDao().getById("archives") shouldBe archives
        }
    }

    @Nested
    inner class SetFolderColor {
        @Test
        fun `should give a folder of the author its colour`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id",
                    name = "my-name",
                    color = FolderColor.MAGENTA,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            repository.setFolderColor(id = "my-folder-id", color = FolderColor.YELLOW)

            // then
            database.folderDao().getById("my-folder-id")?.color shouldBe FolderColor.YELLOW
        }
    }

    @Nested
    inner class DeleteFolder {
        @Test
        fun `should delete the folder and take its songs out of it`() = runTest {
            // given
            database.folderDao().insert(
                FolderEntity(
                    id = "my-folder-id",
                    name = "my-name",
                    color = FolderColor.MAGENTA,
                    position = 0,
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )
            database.songFolderDao().insert(
                SongFolderEntity(
                    id = "my-song-id:my-folder-id",
                    songId = "my-song-id",
                    folderId = "my-folder-id",
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                ),
            )

            // when
            repository.deleteFolder("my-folder-id")

            // then
            database.folderDao().getById("my-folder-id")?.deleted shouldBe true
            database.songFolderDao().getById("my-song-id:my-folder-id")?.let { it.deleted to it.updatedAt } shouldBe
                (true to Instant.parse("2026-11-17T10:00:00Z"))
        }

        @Test
        fun `should keep Archives`() = runTest {
            // given
            val archives = FolderEntity(
                id = "archives",
                name = "",
                color = FolderColor.NEUTRAL,
                kind = FolderKind.ARCHIVES,
                position = 0,
                updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
            )
            database.folderDao().insert(archives)

            // when
            repository.deleteFolder("archives")

            // then
            database.folderDao().getById("archives") shouldBe archives
        }
    }

    @Nested
    inner class FileSongs {
        @Test
        fun `should file the songs in the folders added`() = runTest {
            // when
            repository.fileSongs(
                songIds = listOf("my-song-id-1", "my-song-id-2"),
                add = setOf("my-folder-id"),
                remove = emptySet(),
            )

            // then
            database.songFolderDao().getById("my-song-id-2:my-folder-id") shouldBe SongFolderEntity(
                id = "my-song-id-2:my-folder-id",
                songId = "my-song-id-2",
                folderId = "my-folder-id",
                updatedAt = Instant.parse("2026-11-17T10:00:00Z"),
            )
            database.songFolderDao().getById("my-song-id-1:my-folder-id")?.deleted shouldBe false
        }

        @Test
        fun `should take the songs out of the folders removed and keep them in the others`() = runTest {
            // given
            listOf("my-folder-id-1", "my-folder-id-2").forEach { folderId ->
                database.songFolderDao().insert(
                    SongFolderEntity(
                        id = "my-song-id:$folderId",
                        songId = "my-song-id",
                        folderId = folderId,
                        updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    ),
                )
            }

            // when
            repository.fileSongs(songIds = listOf("my-song-id"), add = emptySet(), remove = setOf("my-folder-id-1"))

            // then
            database.songFolderDao().getById("my-song-id:my-folder-id-1")?.deleted shouldBe true
            database.songFolderDao().getById("my-song-id:my-folder-id-2")?.deleted shouldBe false
        }

        @Test
        fun `should file again a song taken out of a folder`() = runTest {
            // given
            database.songFolderDao().insert(
                SongFolderEntity(
                    id = "my-song-id:my-folder-id",
                    songId = "my-song-id",
                    folderId = "my-folder-id",
                    updatedAt = Instant.parse("2026-11-01T10:00:00Z"),
                    deleted = true,
                ),
            )

            // when
            repository.fileSongs(songIds = listOf("my-song-id"), add = setOf("my-folder-id"), remove = emptySet())

            // then
            database.songFolderDao().getById("my-song-id:my-folder-id")?.deleted shouldBe false
        }
    }
}
