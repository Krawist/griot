package com.griot.feature.library.tidy

import com.griot.core.ai.SongRequest
import com.griot.core.data.model.BuiltInGenre
import com.griot.core.data.model.Folder
import com.griot.core.data.model.Genre
import com.griot.core.data.model.LibrarySong
import com.griot.core.data.model.SongLanguage
import com.griot.core.phonetics.PhoneticLanguage

// The model reads this many verses of a song: enough for its themes and style.
private const val LYRICS_LINES = 12

/** What the AI is asked about the song: its first verses, and the [folders] and [genres] it may pick from. */
internal fun LibrarySong.request(folders: List<Folder> = emptyList(), genres: List<Genre> = emptyList()) = SongRequest(
    language = if (language == SongLanguage.ENGLISH) PhoneticLanguage.ENGLISH else PhoneticLanguage.FRENCH,
    lyrics = firstSection.filter { it.isNotBlank() }.ifEmpty { listOfNotNull(firstVerse) }.take(LYRICS_LINES),
    folders = folders.map { it.name },
    genres = genres.map { it.modelName() },
)

/** The name a model reads: the author's, or the built-in genre's English one. */
internal fun Genre.modelName(): String = when (builtIn) {
    null -> name
    BuiltInGenre.AFRO -> "Afro"
    BuiltInGenre.RAP -> "Rap"
    BuiltInGenre.RNB -> "R&B"
    BuiltInGenre.GOSPEL -> "Gospel"
    BuiltInGenre.POP -> "Pop"
    BuiltInGenre.OTHER -> "Other"
}
