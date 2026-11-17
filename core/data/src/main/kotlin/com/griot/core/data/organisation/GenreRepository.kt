package com.griot.core.data.organisation

import com.griot.core.data.model.Genre
import kotlinx.coroutines.flow.Flow

/** The genres songs are tagged with, synced like the songs: a built-in list the author can extend. */
interface GenreRepository {
    /** The built-in genres, then the author's, in order. */
    fun observeGenres(): Flow<List<Genre>>

    /** Creates a genre named [name] after the others, and returns its id. */
    suspend fun createGenre(name: String): String

    /** Renames the author's genre [id]; a built-in genre keeps its name. */
    suspend fun renameGenre(id: String, name: String)

    /** Deletes the author's genre [id] and removes it from its songs; a built-in genre stays. */
    suspend fun deleteGenre(id: String)
}
