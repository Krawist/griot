package com.griot.feature.library

import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.design.format.relativeTime
import com.griot.core.phonetics.PhoneticEngines
import com.griot.core.phonetics.PhoneticLanguage
import com.griot.core.phonetics.rhymeLetters
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [LibraryItems]. */
@Singleton
internal class LibraryItemsImpl @Inject constructor(private val engines: PhoneticEngines, private val clock: Clock) :
    LibraryItems {
    override fun itemOf(song: LibrarySong, folders: List<Folder>, genres: List<Genre>, now: Instant) = LibraryItem(
        song = song,
        updated = relativeTime(song.updatedAt, now = now, zone = clock.zone),
        rhymes = rhymesOf(song),
        folder = folders.firstOrNull { it.id == song.folderId },
        genre = genres.firstOrNull { it.id == song.genreId },
    )

    // The rhyme ranks of the first section, read in English for an English song and in French otherwise: each engine
    // reads the other language's words.
    private fun rhymesOf(song: LibrarySong): List<Int> {
        val language = if (song.language == SongLanguage.ENGLISH) PhoneticLanguage.ENGLISH else PhoneticLanguage.FRENCH
        val engine = engines.of(language)
        return rhymeLetters(song.firstSection.map { engine.rhyme(it) }).filterNotNull()
    }
}
