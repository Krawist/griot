package com.griot.core.data.song

import com.griot.core.data.database.dao.FavoriteDao
import com.griot.core.data.database.dao.LibrarySongRow
import com.griot.core.data.database.dao.SectionDao
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.dao.SongGenreDao
import com.griot.core.data.database.dao.VerseDao
import com.griot.core.data.database.entity.FavoriteEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongGenreEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.language.SongLanguageDetector
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.NewSection
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongStatus
import com.griot.core.data.model.TrashedSong
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository.Companion.TRASH_DAYS
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default implementation for [SongRepository], on the Room database. */
// One function per change of the library: create, import, detect, tag, rename, duplicate, delete, restore, purge.
@Suppress("TooManyFunctions", "LongParameterList") // And one DAO per table it writes.
@Singleton
internal class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val sectionDao: SectionDao,
    private val verseDao: VerseDao,
    private val songGenreDao: SongGenreDao,
    private val favoriteDao: FavoriteDao,
    private val preferences: UserPreferencesRepository,
    private val detector: SongLanguageDetector,
    private val clock: Clock,
) : SongRepository {
    override fun observeLibrary(): Flow<List<LibrarySong>> =
        combine(songDao.observeLibrary(), songDao.observeFirstSections()) { rows, firstSections ->
            val verses = firstSections.groupBy(keySelector = { it.songId }, valueTransform = { it.text })
            rows.map { it.toLibrarySong(firstSection = verses[it.id].orEmpty()) }
        }

    override fun observeTrash(): Flow<List<TrashedSong>> = songDao.observeTrash().map { rows ->
        rows.map { TrashedSong(id = it.id, title = it.title, deletedAt = it.deletedAt) }
    }

    override suspend fun createSong(firstVerse: String): String {
        val now = Instant.now(clock)
        val song = SongEntity(
            id = newId(),
            title = "",
            language = preferences.defaultSongLanguage.first(),
            status = SongStatus.DRAFT,
            createdAt = now,
            updatedAt = now,
        )
        val section = SectionEntity(
            id = newId(),
            songId = song.id,
            kind = SectionKind.VERSE,
            position = 0,
            updatedAt = now,
        )
        val verse = VerseEntity(
            id = newId(),
            sectionId = section.id,
            text = firstVerse,
            position = 0,
            updatedAt = now,
        )
        songDao.insertWithContent(song = song, sections = listOf(section), verses = listOf(verse))
        return song.id
    }

    override suspend fun importSong(title: String, sections: List<NewSection>): String {
        val now = Instant.now(clock)
        val lyrics = sections.flatMap { it.lines }.joinToString(separator = "\n")
        val song = SongEntity(
            id = newId(),
            title = title,
            language = detector.detect(lyrics) ?: preferences.defaultSongLanguage.first(),
            languageDetected = true,
            status = SongStatus.IN_PROGRESS,
            createdAt = now,
            updatedAt = now,
        )
        val (entities, verses) = newSectionRows(songId = song.id, sections = sections, now = now)
        // A repeat always points to a section before it: inserted in order, its source already exists.
        songDao.insertWithContent(song = song, sections = entities, verses = verses)
        return song.id
    }

    override suspend fun detectLanguages() {
        songDao.getDetected().forEach { song ->
            val lyrics = verseDao.getBySong(song.id).joinToString(separator = "\n") { it.text }
            val language = detector.detect(lyrics)
            if (language != null && language != song.language) {
                songDao.setLanguage(id = song.id, language = language, detected = true, at = Instant.now(clock))
            }
        }
    }

    override suspend fun setFavorite(ids: List<String>, favorite: Boolean) {
        val now = Instant.now(clock)
        ids.forEach { favoriteDao.upsert(FavoriteEntity(songId = it, updatedAt = now, deleted = !favorite)) }
    }

    override suspend fun renameSong(id: String, title: String) {
        songDao.rename(id = id, title = title, at = Instant.now(clock))
    }

    override suspend fun duplicateSong(id: String, title: String): String {
        val original = requireNotNull(songDao.getById(id)) { "No song $id to duplicate" }
        val now = Instant.now(clock)
        val copy = original.copy(id = newId(), title = title, createdAt = now, updatedAt = now, deleted = false)
        val sections = sectionDao.getBySong(id)
        val sectionIds = sections.associate { it.id to newId() }
        val copiedSections = sections
            // A repeat is inserted after the section it repeats, which must exist first.
            .sortedBy { it.repeatOfSectionId != null }
            .map { section ->
                section.copy(
                    id = sectionIds.getValue(section.id),
                    songId = copy.id,
                    repeatOfSectionId = section.repeatOfSectionId?.let(sectionIds::get),
                    updatedAt = now,
                )
            }
        val copiedVerses = verseDao.getBySong(id).map { verse ->
            verse.copy(id = newId(), sectionId = sectionIds.getValue(verse.sectionId), updatedAt = now)
        }
        songDao.insertWithContent(song = copy, sections = copiedSections, verses = copiedVerses)
        // The copy keeps the genres of the original.
        songGenreDao.genresOf(id).forEach { genreId ->
            songGenreDao.upsert(
                SongGenreEntity(
                    id = SongGenreEntity.idOf(songId = copy.id, genreId = genreId),
                    songId = copy.id,
                    genreId = genreId,
                    updatedAt = now,
                ),
            )
        }
        return copy.id
    }

    override suspend fun deleteSong(id: String) {
        songDao.markDeleted(id = id, at = Instant.now(clock))
    }

    override suspend fun restoreSong(id: String) {
        songDao.restore(id = id, at = Instant.now(clock))
    }

    override suspend fun purgeTrash() {
        songDao.purgeDeletedBefore(Instant.now(clock).minus(Duration.ofDays(TRASH_DAYS)))
    }

    private fun newId() = UUID.randomUUID().toString()

    private fun LibrarySongRow.toLibrarySong(firstSection: List<String>) = LibrarySong(
        id = id,
        title = title,
        language = language,
        status = status,
        folderIds = folderIds?.split(',').orEmpty(),
        genreIds = genreIds?.split(',').orEmpty(),
        favorite = favorite,
        updatedAt = updatedAt,
        createdAt = createdAt,
        sectionCount = sectionCount,
        firstVerse = firstVerse,
        lyrics = lyrics.orEmpty(),
        firstSection = firstSection,
    )
}
