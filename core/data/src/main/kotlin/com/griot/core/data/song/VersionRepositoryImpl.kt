package com.griot.core.data.song

import com.griot.core.data.database.dao.ConflictDao
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.dao.VersionDao
import com.griot.core.data.database.entity.VersionEntity
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SongSnapshot
import com.griot.core.data.model.SongVersion
import com.griot.core.data.model.VerseVersion
import com.griot.core.data.model.VersionSource
import com.griot.core.data.song.VersionRepository.Companion.KEEP_ALL_DAYS
import com.griot.core.data.song.VersionRepository.Companion.SONG_INTERVAL_MINUTES
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/** Default implementation for [VersionRepository], on the Room database; a song version holds its snapshot in JSON. */
// One function per way the history changes, and the snapshot of a song.
@Suppress("TooManyFunctions")
@Singleton
internal class VersionRepositoryImpl @Inject constructor(
    private val versionDao: VersionDao,
    private val sectionDao: SectionDao,
    private val verseDao: VerseDao,
    private val conflictDao: ConflictDao,
    private val editor: SongEditorRepository,
    private val clock: Clock,
) : VersionRepository {
    override fun observeVerseHistory(verseId: String): Flow<List<VerseVersion>> =
        versionDao.observeByVerse(verseId).map { versions ->
            versions.map { VerseVersion(id = it.id, text = it.text, source = it.source, createdAt = it.createdAt) }
        }

    override fun observeSongHistory(songId: String): Flow<List<SongVersion>> =
        versionDao.observeBySong(songId).map { versions ->
            versions.map {
                SongVersion(id = it.id, snapshot = decode(it.text), source = it.source, createdAt = it.createdAt)
            }
        }

    override fun observeConflicts(songId: String): Flow<List<String>> = conflictDao.observeVersesBySong(songId)

    override suspend fun dismissVerseConflicts(verseId: String) = conflictDao.deleteByVerse(verseId)

    override suspend fun dismissSongConflicts(songId: String) = conflictDao.deleteBySong(songId)

    override suspend fun recordVerse(verseId: String, source: VersionSource) {
        val verse = verseDao.getById(verseId) ?: return
        val songId = sectionDao.getById(verse.sectionId)?.songId
        if (songId != null && versionDao.latestForVerse(verseId)?.text != verse.text) {
            versionDao.insert(version(songId = songId, verseId = verseId, text = verse.text, source = source))
        }
    }

    override suspend fun recordSong(songId: String) {
        val last = versionDao.latestForSong(songId)
        val recent =
            last != null && Duration.between(last.createdAt, Instant.now(clock)).toMinutes() < SONG_INTERVAL_MINUTES
        if (!recent) record(songId = songId, source = VersionSource.AUTHOR)
    }

    override suspend fun restoreVerse(versionId: String) {
        val version = versionDao.getById(versionId) ?: return
        val verseId = version.verseId ?: return
        recordVerse(verseId)
        editor.updateVerse(verseId = verseId, text = version.text)
        recordVerse(verseId = verseId, source = VersionSource.RESTORE)
    }

    override suspend fun restoreSong(versionId: String) {
        val version = versionDao.getById(versionId) ?: return
        record(songId = version.songId, source = VersionSource.AUTHOR)
        editor.replaceContent(songId = version.songId, sections = decode(version.text).sections)
        record(songId = version.songId, source = VersionSource.RESTORE)
    }

    override suspend fun thin() {
        val now = Instant.now(clock)
        val old = versionDao.recordedBefore(now.minus(Duration.ofDays(KEEP_ALL_DAYS)))
        val dropped = old
            .groupBy { (it.verseId ?: "song:${it.songId}") to it.createdAt.atZone(clock.zone).toLocalDate() }
            .values
            .flatMap { day -> day.sortedBy { it.createdAt }.dropLast(1) }
            .map { it.id }
        if (dropped.isNotEmpty()) versionDao.markDeleted(ids = dropped, at = now)
    }

    // The song as it is now, unless it is the last version already.
    private suspend fun record(songId: String, source: VersionSource) {
        val text = Json.encodeToString(SongSnapshot.serializer(), snapshot(songId))
        val last = versionDao.latestForSong(songId)
        if (last?.text != text || source == VersionSource.RESTORE) {
            versionDao.insert(version(songId = songId, verseId = null, text = text, source = source))
        }
    }

    private suspend fun snapshot(songId: String): SongSnapshot {
        val sections = sectionDao.getBySong(songId)
        val verses = verseDao.getBySong(songId).groupBy { it.sectionId }
        return SongSnapshot(
            sections = sections.map { section ->
                NewSection(
                    kind = section.kind,
                    name = section.name,
                    lines = verses[section.id].orEmpty().map { it.text },
                    repeatOf = section.repeatOfSectionId?.let { source -> sections.indexOfFirst { it.id == source } }
                        ?.takeIf { it >= 0 },
                )
            },
        )
    }

    private fun decode(text: String): SongSnapshot = Json.decodeFromString(SongSnapshot.serializer(), text)

    private fun version(songId: String, verseId: String?, text: String, source: VersionSource): VersionEntity {
        val now = Instant.now(clock)
        return VersionEntity(
            id = UUID.randomUUID().toString(),
            songId = songId,
            verseId = verseId,
            text = text,
            source = source,
            createdAt = now,
            updatedAt = now,
        )
    }
}
