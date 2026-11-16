package com.griot.core.data.song

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongSection
import com.griot.core.data.model.SongVerse
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Default implementation for [SongEditorRepository], on the Room database, one transaction per change. */
@Singleton
internal class SongEditorRepositoryImpl @Inject constructor(
    private val database: GriotDatabase,
    private val songDao: SongDao,
    private val sectionDao: SectionDao,
    private val verseDao: VerseDao,
    private val clock: Clock,
) : SongEditorRepository {
    override fun observeDocument(songId: String): Flow<SongDocument?> = combine(
        songDao.observeById(songId),
        sectionDao.observeBySong(songId),
        verseDao.observeBySong(songId),
    ) { song, sections, verses ->
        song?.takeUnless { it.deleted }?.let {
            val versesBySection = verses.groupBy { verse -> verse.sectionId }
            SongDocument(
                id = it.id,
                title = it.title,
                language = it.language,
                sections = sections.map { section ->
                    SongSection(
                        id = section.id,
                        kind = section.kind,
                        name = section.name,
                        repeatOf = section.repeatOfSectionId,
                        verses = versesBySection[section.repeatOfSectionId ?: section.id].orEmpty()
                            .map { verse -> SongVerse(id = verse.id, text = verse.text) },
                    )
                },
            )
        }
    }

    override suspend fun addSection(songId: String, kind: SectionKind): String = write { now ->
        val section = SectionEntity(
            id = newId(),
            songId = songId,
            kind = kind,
            position = sectionDao.getBySong(songId).size,
            updatedAt = now,
        )
        sectionDao.insert(section)
        verseDao.insert(VerseEntity(id = newId(), sectionId = section.id, text = "", position = 0, updatedAt = now))
        songDao.touch(id = songId, at = now)
        section.id
    }

    override suspend fun repeatSection(sectionId: String): String = write { now ->
        val section = requireSection(sectionId)
        val repeat = SectionEntity(
            id = newId(),
            songId = section.songId,
            kind = section.kind,
            position = sectionDao.getBySong(section.songId).size,
            repeatOfSectionId = section.repeatOfSectionId ?: section.id,
            updatedAt = now,
        )
        sectionDao.insert(repeat)
        songDao.touch(id = section.songId, at = now)
        repeat.id
    }

    override suspend fun renameSection(sectionId: String, name: String?) = write { now ->
        val section = requireSection(sectionId)
        sectionDao.rename(id = sectionId, name = name?.trim()?.takeIf { it.isNotEmpty() }, at = now)
        songDao.touch(id = section.songId, at = now)
    }

    override suspend fun moveSection(sectionId: String, offset: Int) = write { now ->
        val section = requireSection(sectionId)
        val sections = sectionDao.getBySong(section.songId).toMutableList()
        val from = sections.indexOfFirst { it.id == sectionId }
        val to = (from + offset).coerceIn(0, sections.lastIndex)
        if (from != to) {
            sections.add(to, sections.removeAt(from))
            // Positions are rewritten from 0, so gaps left by deleted sections disappear.
            sections.forEachIndexed { position, moved ->
                if (moved.position != position) sectionDao.move(id = moved.id, position = position, at = now)
            }
            songDao.touch(id = section.songId, at = now)
        }
    }

    override suspend fun deleteSection(sectionId: String) = write { now ->
        val section = requireSection(sectionId)
        sectionDao.markDeleted(id = sectionId, at = now)
        sectionDao.markRepeatsDeleted(id = sectionId, at = now)
        songDao.touch(id = section.songId, at = now)
    }

    override suspend fun updateVerse(verseId: String, text: String) = write { now ->
        val verse = requireNotNull(verseDao.getById(verseId)) { "No verse $verseId" }
        val section = requireSection(verse.sectionId)
        verseDao.updateText(id = verseId, text = text, at = now)
        songDao.touch(id = section.songId, at = now)
    }

    private suspend fun requireSection(id: String): SectionEntity =
        requireNotNull(sectionDao.getById(id)) { "No section $id" }

    // One transaction per change, dated once: the song and its content change together or not at all.
    private suspend fun <T> write(change: suspend (Instant) -> T): T {
        val now = Instant.now(clock)
        return database.useWriterConnection { transactor -> transactor.immediateTransaction { change(now) } }
    }

    private fun newId() = UUID.randomUUID().toString()
}
