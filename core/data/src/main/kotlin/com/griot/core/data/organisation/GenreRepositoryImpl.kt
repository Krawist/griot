package com.griot.core.data.organisation

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.GenreEntity
import com.griot.core.data.model.Genre
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [GenreRepository], on the Room database. */
@Singleton
internal class GenreRepositoryImpl @Inject constructor(private val database: GriotDatabase, private val clock: Clock) :
    GenreRepository {
    private val genreDao = database.genreDao()

    override fun observeGenres(): Flow<List<Genre>> = genreDao.observeAll().map { rows ->
        rows.map { Genre(id = it.id, name = it.name, builtIn = it.builtIn) }
    }

    override suspend fun createGenre(name: String): String = write { now ->
        val genre = GenreEntity(name = name.trim(), position = genreDao.nextPosition(), updatedAt = now)
        genreDao.insert(genre)
        genre.id
    }

    override suspend fun renameGenre(id: String, name: String) = write { now ->
        if (isCustom(id)) genreDao.rename(id = id, name = name.trim(), at = now)
    }

    override suspend fun deleteGenre(id: String) = write { now ->
        if (isCustom(id)) {
            genreDao.markDeleted(id = id, at = now)
            database.songDao().clearGenre(genreId = id, at = now)
        }
    }

    // A genre of the author: the built-in ones cannot be renamed or deleted.
    private suspend fun isCustom(id: String) = genreDao.getById(id)?.let { it.builtIn == null } == true

    private suspend fun <T> write(change: suspend (Instant) -> T): T {
        val now = Instant.now(clock)
        return database.useWriterConnection { transactor -> transactor.immediateTransaction { change(now) } }
    }
}
