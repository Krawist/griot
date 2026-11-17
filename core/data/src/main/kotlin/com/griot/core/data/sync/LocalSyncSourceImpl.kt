package com.griot.core.data.sync

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.AttachmentEntity
import com.griot.core.data.database.entity.ConflictEntity
import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.database.entity.GenreEntity
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.model.SyncEntityType
import com.griot.core.data.model.SyncRecord
import com.griot.core.data.model.VersionSource
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Default implementation for [LocalSyncSource]. */
@Singleton
internal class LocalSyncSourceImpl @Inject constructor(private val database: GriotDatabase, private val clock: Clock) :
    LocalSyncSource {
    private val syncDao = database.syncDao()

    private val tables: Map<SyncEntityType, Table<*>> = mapOf(
        SyncEntityType.SONG to Table(
            serializer = SongEntity.serializer(),
            get = database.songDao()::getById,
            insert = database.songDao()::insert,
            update = database.songDao()::update,
            updatedAt = SongEntity::updatedAt,
        ),
        SyncEntityType.SECTION to Table(
            serializer = SectionEntity.serializer(),
            get = database.sectionDao()::getById,
            insert = database.sectionDao()::insert,
            update = database.sectionDao()::update,
            updatedAt = SectionEntity::updatedAt,
        ),
        SyncEntityType.VERSE to Table(
            serializer = VerseEntity.serializer(),
            get = database.verseDao()::getById,
            insert = database.verseDao()::insert,
            update = database.verseDao()::update,
            updatedAt = VerseEntity::updatedAt,
            keep = ::keepVerse,
        ),
        SyncEntityType.VERSION to Table(
            serializer = VersionEntity.serializer(),
            get = database.versionDao()::getById,
            insert = database.versionDao()::insert,
            update = database.versionDao()::update,
            updatedAt = VersionEntity::updatedAt,
        ),
        SyncEntityType.IDEA to Table(
            serializer = IdeaEntity.serializer(),
            get = database.ideaDao()::getById,
            insert = database.ideaDao()::insert,
            update = database.ideaDao()::update,
            updatedAt = IdeaEntity::updatedAt,
            keep = ::keepIdea,
        ),
        SyncEntityType.FOLDER to Table(
            serializer = FolderEntity.serializer(),
            get = database.folderDao()::getById,
            insert = database.folderDao()::insert,
            update = database.folderDao()::update,
            updatedAt = FolderEntity::updatedAt,
        ),
        SyncEntityType.GENRE to Table(
            serializer = GenreEntity.serializer(),
            get = database.genreDao()::getById,
            insert = database.genreDao()::insert,
            update = database.genreDao()::update,
            updatedAt = GenreEntity::updatedAt,
        ),
        SyncEntityType.SONG_FOLDER to Table(
            serializer = SongFolderEntity.serializer(),
            get = database.songFolderDao()::getById,
            insert = database.songFolderDao()::insert,
            update = database.songFolderDao()::update,
            updatedAt = SongFolderEntity::updatedAt,
        ),
        SyncEntityType.ATTACHMENT to Table(
            serializer = AttachmentEntity.serializer(),
            get = database.attachmentDao()::getById,
            insert = database.attachmentDao()::insert,
            update = database.attachmentDao()::update,
            updatedAt = AttachmentEntity::updatedAt,
        ),
    )

    override fun observePendingCount(): Flow<Int> = syncDao.observePendingCount()

    override suspend fun pending(): List<SyncRecord> = database.useWriterConnection { transactor ->
        transactor.immediateTransaction {
            syncDao.pendingSongs().map { record(SyncEntityType.SONG, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingSections().map { record(SyncEntityType.SECTION, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingVerses().map { record(SyncEntityType.VERSE, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingVersions().map { record(SyncEntityType.VERSION, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingIdeas().map { record(SyncEntityType.IDEA, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingFolders().map { record(SyncEntityType.FOLDER, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingGenres().map { record(SyncEntityType.GENRE, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingSongFolders().map {
                    record(SyncEntityType.SONG_FOLDER, it.id, it.updatedAt, it.deleted, it)
                } +
                syncDao.pendingAttachments().map {
                    record(SyncEntityType.ATTACHMENT, it.id, it.updatedAt, it.deleted, it)
                }
        }
    }

    override suspend fun markPushed(records: List<SyncRecord>) {
        val now = Instant.now(clock)
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { records.forEach { markSynced(record = it, at = now) } }
        }
    }

    override suspend fun apply(records: List<SyncRecord>) {
        val now = Instant.now(clock)
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                // Parents first, so that a verse never arrives before its section.
                records.sortedBy { it.type.ordinal }.forEach { record ->
                    val synced = database.syncStateDao().get(entityType = record.type, entityId = record.id)
                    val table = checkNotNull(tables[record.type])
                    if (table.take(record = record, syncedUpdatedAt = synced?.syncedUpdatedAt, at = now)) {
                        markSynced(record = record, at = now)
                    }
                }
            }
        }
    }

    // A verse changed on both devices: the text that lost becomes a version of the verse, and a notice.
    private suspend fun keepVerse(winner: VerseEntity, loser: VerseEntity, at: Instant) {
        if (winner.text == loser.text) return
        val songId = database.sectionDao().getById(loser.sectionId)?.songId ?: return
        val version = VersionEntity(
            songId = songId,
            verseId = loser.id,
            text = loser.text,
            source = VersionSource.SYNC_CONFLICT,
            createdAt = at,
            updatedAt = at,
        )
        database.versionDao().insert(version)
        database.conflictDao().insert(
            ConflictEntity(versionId = version.id, songId = songId, verseId = loser.id, createdAt = at),
        )
    }

    // An idea changed on both devices: the text that lost stays as an idea of its own.
    private suspend fun keepIdea(winner: IdeaEntity, loser: IdeaEntity, at: Instant) {
        if (winner.text == loser.text) return
        database.ideaDao().insert(loser.copy(id = UUID.randomUUID().toString(), updatedAt = at, deleted = false))
    }

    private suspend fun markSynced(record: SyncRecord, at: Instant) {
        database.syncStateDao().upsert(
            SyncStateEntity(
                entityType = record.type,
                entityId = record.id,
                syncedUpdatedAt = record.updatedAt,
                syncedAt = at,
            ),
        )
    }

    private inline fun <reified T> record(
        type: SyncEntityType,
        id: String,
        updatedAt: Instant,
        deleted: Boolean,
        row: T,
    ) = SyncRecord(
        type = type,
        id = id,
        updatedAt = updatedAt,
        deleted = deleted,
        payload = json.encodeToString(row),
    )

    /**
     * How the rows of one table are read and written.
     *
     * @property keep Keeps what the losing row of a conflict holds, given the winning row, the losing one and the time;
     * `null` when the most recent row may simply win.
     */
    private class Table<T : Any>(
        val serializer: KSerializer<T>,
        val get: suspend (String) -> T?,
        val insert: suspend (T) -> Unit,
        val update: suspend (T) -> Unit,
        val updatedAt: (T) -> Instant,
        val keep: (suspend (winner: T, loser: T, at: Instant) -> Unit)? = null,
    ) {
        /**
         * Takes the remote row when it is missing here or more recent; when the local row also changed since its last
         * sync ([syncedUpdatedAt]), the losing one is kept first. Returns whether the two rows now agree.
         */
        suspend fun take(record: SyncRecord, syncedUpdatedAt: Instant?, at: Instant): Boolean {
            val remote = json.decodeFromString(serializer, record.payload)
            val local = get(record.id)
            if (local == null) {
                insert(remote)
                return true
            }
            return settle(local = local, remote = remote, syncedUpdatedAt = syncedUpdatedAt, at = at)
        }

        // The most recent of the two rows wins; when both changed since the last sync, the losing one is kept first.
        private suspend fun settle(local: T, remote: T, syncedUpdatedAt: Instant?, at: Instant): Boolean {
            val localUpdatedAt = updatedAt(local)
            val remoteUpdatedAt = updatedAt(remote)
            val remoteWins = remoteUpdatedAt > localUpdatedAt
            val localChanged = syncedUpdatedAt == null || localUpdatedAt > syncedUpdatedAt
            if (remoteUpdatedAt != localUpdatedAt && localChanged) {
                keep?.invoke(if (remoteWins) remote else local, if (remoteWins) local else remote, at)
            }
            if (remoteWins) update(remote)
            return remoteUpdatedAt >= localUpdatedAt
        }
    }

    private companion object {
        // Defaults are written, as an id generated on reading would make another row.
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }
}
