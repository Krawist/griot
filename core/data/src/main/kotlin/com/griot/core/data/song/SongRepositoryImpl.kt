package com.griot.core.data.song

import com.griot.core.data.database.dao.LibrarySongRow
import com.griot.core.data.database.dao.SongDao
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongStatus
import com.griot.core.data.preferences.UserPreferencesRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Default implementation for [SongRepository], on the Room database. */
@Singleton
internal class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : SongRepository {
    override fun observeLibrary(): Flow<List<LibrarySong>> =
        songDao.observeLibrary().map { rows -> rows.map { it.toLibrarySong() } }

    override suspend fun createSong(): String {
        val now = Instant.now(clock)
        val song = SongEntity(
            id = UUID.randomUUID().toString(),
            title = "",
            language = preferences.defaultSongLanguage.first(),
            status = SongStatus.DRAFT,
            createdAt = now,
            updatedAt = now,
        )
        val section = SectionEntity(
            id = UUID.randomUUID().toString(),
            songId = song.id,
            kind = SectionKind.VERSE,
            position = 0,
            updatedAt = now,
        )
        val verse = VerseEntity(
            id = UUID.randomUUID().toString(),
            sectionId = section.id,
            text = "",
            position = 0,
            updatedAt = now,
        )
        songDao.insertWithContent(song = song, sections = listOf(section), verses = listOf(verse))
        return song.id
    }

    private fun LibrarySongRow.toLibrarySong() = LibrarySong(
        id = id,
        title = title,
        language = language,
        status = status,
        updatedAt = updatedAt,
        sectionCount = sectionCount,
        firstVerse = firstVerse,
        lyrics = lyrics.orEmpty(),
    )
}
