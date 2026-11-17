package com.griot.core.data.sync

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SyncStateEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.model.SyncEntityType
import com.griot.core.data.model.SyncRecord
import java.time.Clock
import java.time.Instant
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
        ),
    )

    override fun observePendingCount(): Flow<Int> = syncDao.observePendingCount()

    override suspend fun pending(): List<SyncRecord> = database.useWriterConnection { transactor ->
        transactor.immediateTransaction {
            syncDao.pendingSongs().map { record(SyncEntityType.SONG, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingSections().map { record(SyncEntityType.SECTION, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingVerses().map { record(SyncEntityType.VERSE, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingVersions().map { record(SyncEntityType.VERSION, it.id, it.updatedAt, it.deleted, it) } +
                syncDao.pendingIdeas().map { record(SyncEntityType.IDEA, it.id, it.updatedAt, it.deleted, it) }
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
                    if (checkNotNull(tables[record.type]).take(record)) markSynced(record = record, at = now)
                }
            }
        }
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

    // How the rows of one table are read and written.
    private class Table<T : Any>(
        val serializer: KSerializer<T>,
        val get: suspend (String) -> T?,
        val insert: suspend (T) -> Unit,
        val update: suspend (T) -> Unit,
        val updatedAt: (T) -> Instant,
    ) {
        /** Takes the remote row when it is missing here or more recent; returns whether the two now agree. */
        suspend fun take(record: SyncRecord): Boolean {
            val local = get(record.id)
            val remote = json.decodeFromString(serializer, record.payload)
            when {
                local == null -> insert(remote)
                record.updatedAt > updatedAt(local) -> update(remote)
                record.updatedAt < updatedAt(local) -> return false
            }
            return true
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
