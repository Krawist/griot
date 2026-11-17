package com.griot.core.data.organisation

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.model.Folder
import com.griot.core.data.model.FolderColor
import com.griot.core.data.model.FolderKind
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [FolderRepository], on the Room database. */
@Singleton
internal class FolderRepositoryImpl @Inject constructor(private val database: GriotDatabase, private val clock: Clock) :
    FolderRepository {
    private val folderDao = database.folderDao()
    private val songFolderDao = database.songFolderDao()

    override fun observeFolders(): Flow<List<Folder>> = folderDao.observeWithCounts().map { rows ->
        rows.map { Folder(id = it.id, name = it.name, color = it.color, kind = it.kind, songCount = it.songCount) }
    }

    override suspend fun createFolder(name: String, color: FolderColor): String = write { now ->
        val folder =
            FolderEntity(name = name.trim(), color = color, position = folderDao.nextPosition(), updatedAt = now)
        folderDao.insert(folder)
        folder.id
    }

    override suspend fun renameFolder(id: String, name: String) = write { now ->
        if (isCustom(id)) folderDao.rename(id = id, name = name.trim(), at = now)
    }

    override suspend fun setFolderColor(id: String, color: FolderColor) = write { now ->
        if (isCustom(id)) folderDao.setColor(id = id, color = color, at = now)
    }

    override suspend fun deleteFolder(id: String) = write { now ->
        if (isCustom(id)) {
            folderDao.markDeleted(id = id, at = now)
            songFolderDao.removeFolder(folderId = id, at = now)
        }
    }

    override suspend fun fileSongs(songIds: List<String>, add: Set<String>, remove: Set<String>) = write { now ->
        add.forEach { folderId ->
            songIds.forEach { songId ->
                songFolderDao.upsert(
                    SongFolderEntity(
                        id = SongFolderEntity.idOf(songId = songId, folderId = folderId),
                        songId = songId,
                        folderId = folderId,
                        updatedAt = now,
                    ),
                )
            }
        }
        remove.forEach { songFolderDao.remove(songIds = songIds, folderId = it, at = now) }
    }

    private suspend fun isCustom(id: String) = folderDao.getById(id)?.kind == FolderKind.CUSTOM

    private suspend fun <T> write(change: suspend (Instant) -> T): T {
        val now = Instant.now(clock)
        return database.useWriterConnection { transactor -> transactor.immediateTransaction { change(now) } }
    }
}
