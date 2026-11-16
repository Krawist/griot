package com.griot.core.data.song

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.PastedSections
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
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
// One function per change the editor makes.
@Suppress("TooManyFunctions")
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

    override suspend fun setLanguage(songId: String, language: SongLanguage) = write { now ->
        songDao.setLanguage(id = songId, language = language, at = now)
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

    override suspend fun appendVerse(songId: String, text: String) = write { now ->
        val sections = sectionDao.getBySong(songId)
        val target = sections.lastOrNull { it.repeatOfSectionId == null } ?: SectionEntity(
            id = newId(),
            songId = songId,
            kind = SectionKind.VERSE,
            position = sections.size,
            updatedAt = now,
        ).also { sectionDao.insert(it) }
        val verses = verseDao.getBySection(target.id)
        verseDao.insert(
            VerseEntity(
                id = newId(),
                sectionId = target.id,
                text = text,
                position = (verses.maxOfOrNull { it.position } ?: -1) + 1,
                updatedAt = now,
            ),
        )
        songDao.touch(id = songId, at = now)
    }

    override suspend fun updateVerse(verseId: String, text: String) = write { now ->
        val section = requireSection(requireVerse(verseId).sectionId)
        verseDao.updateText(id = verseId, text = text, at = now)
        songDao.touch(id = section.songId, at = now)
    }

    override suspend fun splitVerse(verseId: String, lines: List<String>): List<String> = write { now ->
        val verse = requireVerse(verseId)
        songDao.touch(id = requireSection(verse.sectionId).songId, at = now)
        split(verse = verse, lines = lines, now = now)
    }

    override suspend fun pasteSections(verseId: String, sections: List<NewSection>): PastedSections = write { now ->
        val verse = requireVerse(verseId)
        val section = requireSection(verse.sectionId)
        val verseIds = split(verse = verse, lines = sections.first().lines.ifEmpty { listOf("") }, now = now)
        val created = sections.drop(1).mapIndexed { index, new ->
            SectionEntity(
                id = newId(),
                songId = section.songId,
                kind = new.kind,
                name = new.name,
                position = section.position + index + 1,
                updatedAt = now,
            )
        }
        val ids = listOf(section.id) + created.map { it.id }
        repeat(created.size) { sectionDao.shiftAfter(songId = section.songId, position = section.position, at = now) }
        created.forEachIndexed { index, entity ->
            val new = sections[index + 1]
            sectionDao.insert(entity.copy(repeatOfSectionId = new.repeatOf?.let(ids::get)))
            if (new.repeatOf == null) {
                verseDao.insertAll(
                    new.lines.mapIndexed { position, text ->
                        VerseEntity(
                            id = newId(),
                            sectionId = entity.id,
                            text = text,
                            position = position,
                            updatedAt = now,
                        )
                    },
                )
            }
        }
        songDao.touch(id = section.songId, at = now)
        PastedSections(sectionIds = created.map { it.id }, verseIds = verseIds)
    }

    override suspend fun replaceContent(songId: String, sections: List<NewSection>) = write { now ->
        sectionDao.getBySong(songId).forEach { sectionDao.markDeleted(id = it.id, at = now) }
        val (rows, verses) = newSectionRows(songId = songId, sections = sections, now = now)
        rows.forEach { sectionDao.insert(it) }
        verseDao.insertAll(verses)
        songDao.touch(id = songId, at = now)
    }

    override suspend fun undoPaste(paste: PastedSections, verseId: String, lines: List<String>) = write { now ->
        paste.sectionIds.forEach { sectionDao.markDeleted(id = it, at = now) }
        paste.verseIds.forEach { verseDao.markDeleted(id = it, at = now) }
        val verse = requireVerse(verseId)
        songDao.touch(id = requireSection(verse.sectionId).songId, at = now)
        split(verse = verse, lines = lines, now = now)
        Unit
    }

    // The verse keeps the first line; each other line becomes a verse below it. Returns the ids of the new verses.
    private suspend fun split(verse: VerseEntity, lines: List<String>, now: Instant): List<String> {
        val added = lines.drop(1).mapIndexed { index, text ->
            VerseEntity(
                id = newId(),
                sectionId = verse.sectionId,
                text = text,
                position = verse.position + index + 1,
                updatedAt = now,
            )
        }
        verseDao.shiftAfter(sectionId = verse.sectionId, position = verse.position, count = added.size, at = now)
        verseDao.updateText(id = verse.id, text = lines.first(), at = now)
        verseDao.insertAll(added)
        return added.map { it.id }
    }

    override suspend fun mergeVerses(previousId: String, verseId: String) = write { now ->
        val previous = requireVerse(previousId)
        val verse = requireVerse(verseId)
        verseDao.updateText(id = previousId, text = previous.text + verse.text, at = now)
        verseDao.markDeleted(id = verseId, at = now)
        songDao.touch(id = requireSection(verse.sectionId).songId, at = now)
    }

    override suspend fun closeSection(verseId: String): String = write { now ->
        val verse = requireVerse(verseId)
        val section = requireSection(verse.sectionId)
        verseDao.markDeleted(id = verseId, at = now)
        sectionDao.shiftAfter(songId = section.songId, position = section.position, at = now)
        val next = SectionEntity(
            id = newId(),
            songId = section.songId,
            kind = SectionKind.VERSE,
            position = section.position + 1,
            updatedAt = now,
        )
        val first = VerseEntity(id = newId(), sectionId = next.id, text = "", position = 0, updatedAt = now)
        sectionDao.insert(next)
        verseDao.insert(first)
        songDao.touch(id = section.songId, at = now)
        first.id
    }

    private suspend fun requireVerse(id: String): VerseEntity = requireNotNull(verseDao.getById(id)) { "No verse $id" }

    private suspend fun requireSection(id: String): SectionEntity =
        requireNotNull(sectionDao.getById(id)) { "No section $id" }

    // One transaction per change, dated once: the song and its content change together or not at all.
    private suspend fun <T> write(change: suspend (Instant) -> T): T {
        val now = Instant.now(clock)
        return database.useWriterConnection { transactor -> transactor.immediateTransaction { change(now) } }
    }

    private fun newId() = UUID.randomUUID().toString()
}
