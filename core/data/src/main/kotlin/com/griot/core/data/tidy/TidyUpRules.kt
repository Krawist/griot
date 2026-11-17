package com.griot.core.data.tidy

import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongStatus
import com.griot.core.data.organisation.FolderRepository
import java.text.Normalizer
import java.time.Duration
import java.time.Instant
import java.util.Locale

private val Accents = Regex("\\p{Mn}+")
private val Spaces = Regex("\\s+")
private val Punctuation = Regex("[^\\p{L}\\p{N} ]")

/** A draft untouched for longer than this is proposed for « Archives ». */
val STALE_DRAFT_AGE: Duration = Duration.ofDays(30)

/**
 * The groups of songs with the same text, found by code (never by a model): case, accents, punctuation, spaces and
 * the order of the verses are ignored. Songs without text are never duplicates. Each group keeps the order of
 * [songs], so the last changed comes first when they are sorted so.
 */
fun findDuplicates(songs: List<LibrarySong>): List<List<LibrarySong>> = songs
    .filter { it.lyrics.isNotBlank() }
    .groupBy { it.lyrics.normalisedText() }
    .values
    .filter { it.size > 1 }

/** The drafts untouched for more than 30 days at [now], not in « Archives » yet. */
fun staleDrafts(songs: List<LibrarySong>, now: Instant): List<LibrarySong> = songs.filter { song ->
    song.status == SongStatus.DRAFT &&
        Duration.between(song.updatedAt, now) > STALE_DRAFT_AGE &&
        FolderRepository.ARCHIVES_ID !in song.folderIds
}

// The verses, each normalised, sorted: the library reads them in no particular order.
private fun String.normalisedText(): String = lines()
    .map { line ->
        Normalizer.normalize(line, Normalizer.Form.NFD)
            .replace(Accents, "")
            .lowercase(Locale.ROOT)
            .replace(Punctuation, " ")
            .replace(Spaces, " ")
            .trim()
    }
    .filter { it.isNotEmpty() }
    .sorted()
    .joinToString(separator = "\n")
