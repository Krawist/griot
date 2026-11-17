package com.griot.core.data.tidy

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection
import com.griot.core.data.database.GriotDatabase
import com.griot.core.data.database.entity.FavoriteEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.entity.SongGenreEntity
import com.griot.core.data.database.entity.TidyRefusalEntity
import com.griot.core.data.model.TidyUpCandidates
import com.griot.core.data.model.TidyUpKind
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

/** Default implementation for [TidyUpRepository]. */
@Singleton
internal class TidyUpRepositoryImpl @Inject constructor(
    private val database: GriotDatabase,
    private val songs: SongRepository,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : TidyUpRepository {
    override fun observeDue(): Flow<Boolean> =
        combine(preferences.tidyUpRequested, preferences.tidyUpReviewedAt) { requested, reviewedAt ->
            requested || reviewedAt == null || Duration.between(reviewedAt, Instant.now(clock)) >= EVERY
        }

    override fun observeCandidates(): Flow<TidyUpCandidates> =
        combine(songs.observeLibrary(), database.tidyRefusalDao().observeIds()) { library, refused ->
            val refusals = refused.toSet()
            fun allowed(kind: TidyUpKind, key: String) = "$kind:$key" !in refusals
            TidyUpCandidates(
                duplicates = findDuplicates(library).filter { group ->
                    allowed(TidyUpKind.DUPLICATE, TidyUpRepository.duplicateKey(group.map { it.id }))
                },
                staleDrafts = staleDrafts(library, Instant.now(clock)).filter { allowed(TidyUpKind.ARCHIVE, it.id) },
                untitled = library.filter {
                    it.title.isBlank() &&
                        it.firstVerse != null &&
                        allowed(TidyUpKind.TITLE, it.id)
                },
                unclassified = library.filter {
                    it.folderIds.isEmpty() && it.genreIds.isEmpty() && allowed(TidyUpKind.CLASSIFY, it.id)
                },
            )
        }

    override suspend fun refuse(kind: TidyUpKind, keys: List<String>) {
        val now = Instant.now(clock)
        database.tidyRefusalDao().insertAll(
            keys.map { TidyRefusalEntity(id = "$kind:$it", kind = kind, key = it, createdAt = now) },
        )
    }

    override suspend fun mergeDuplicates(keepId: String, duplicates: List<String>) {
        val now = Instant.now(clock)
        val library = songs.observeLibrary().first()
        val merged = library.filter { it.id in duplicates }
        val folders = merged.flatMap { it.folderIds }.toSet()
        val genres = merged.flatMap { it.genreIds }.toSet()
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction {
                duplicates.forEach { id ->
                    database.versionDao().moveToSong(from = id, to = keepId, at = now)
                    database.songDao().markDeleted(id = id, at = now)
                }
                folders.forEach { folderId ->
                    database.songFolderDao().upsert(
                        SongFolderEntity(
                            id = SongFolderEntity.idOf(songId = keepId, folderId = folderId),
                            songId = keepId,
                            folderId = folderId,
                            updatedAt = now,
                        ),
                    )
                }
                genres.forEach { genreId ->
                    database.songGenreDao().upsert(
                        SongGenreEntity(
                            id = SongGenreEntity.idOf(songId = keepId, genreId = genreId),
                            songId = keepId,
                            genreId = genreId,
                            updatedAt = now,
                        ),
                    )
                }
                // A duplicate in the favourites puts the song kept there.
                if (merged.any { it.favorite }) {
                    database.favoriteDao().upsert(FavoriteEntity(songId = keepId, updatedAt = now))
                }
            }
        }
    }

    override suspend fun markReviewed() {
        preferences.setTidyUpReviewed(Instant.now(clock))
    }

    private companion object {
        val EVERY: Duration = Duration.ofDays(7)
    }
}
