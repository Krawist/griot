package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.griot.core.data.database.entity.AttachmentEntity
import com.griot.core.data.database.entity.FavoriteEntity
import com.griot.core.data.database.entity.FolderEntity
import com.griot.core.data.database.entity.GenreEntity
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.SongEntity
import com.griot.core.data.database.entity.SongFolderEntity
import com.griot.core.data.database.entity.SongGenreEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.database.entity.VersionEntity
import kotlinx.coroutines.flow.Flow

/**
 * The rows with changes to push: those never synced, and those changed since their last sync. Deleted rows are
 * included, so that deletes travel too.
 */
@Suppress("TooManyFunctions") // One query of changes to push per table.
@Dao
internal interface SyncDao {
    @Query(
        """
        SELECT t.* FROM songs t LEFT JOIN sync_state s ON s.entityType = 'SONG' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingSongs(): List<SongEntity>

    @Query(
        """
        SELECT t.* FROM sections t LEFT JOIN sync_state s ON s.entityType = 'SECTION' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingSections(): List<SectionEntity>

    @Query(
        """
        SELECT t.* FROM verses t LEFT JOIN sync_state s ON s.entityType = 'VERSE' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingVerses(): List<VerseEntity>

    @Query(
        """
        SELECT t.* FROM versions t LEFT JOIN sync_state s ON s.entityType = 'VERSION' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingVersions(): List<VersionEntity>

    @Query(
        """
        SELECT t.* FROM ideas t LEFT JOIN sync_state s ON s.entityType = 'IDEA' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingIdeas(): List<IdeaEntity>

    @Query(
        """
        SELECT t.* FROM folders t LEFT JOIN sync_state s ON s.entityType = 'FOLDER' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingFolders(): List<FolderEntity>

    @Query(
        """
        SELECT t.* FROM genres t LEFT JOIN sync_state s ON s.entityType = 'GENRE' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingGenres(): List<GenreEntity>

    @Query(
        """
        SELECT t.* FROM song_folders t LEFT JOIN sync_state s ON s.entityType = 'SONG_FOLDER' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingSongFolders(): List<SongFolderEntity>

    @Query(
        """
        SELECT t.* FROM attachments t LEFT JOIN sync_state s ON s.entityType = 'ATTACHMENT' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingAttachments(): List<AttachmentEntity>

    @Query(
        """
        SELECT t.* FROM song_genres t LEFT JOIN sync_state s ON s.entityType = 'SONG_GENRE' AND s.entityId = t.id
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingSongGenres(): List<SongGenreEntity>

    @Query(
        """
        SELECT t.* FROM favorites t LEFT JOIN sync_state s ON s.entityType = 'FAVORITE' AND s.entityId = t.songId
        WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt
        """,
    )
    suspend fun pendingFavorites(): List<FavoriteEntity>

    /** How many rows have changes to push, updated on every write. */
    @Query(
        """
        SELECT
        (SELECT COUNT(*) FROM songs t LEFT JOIN sync_state s ON s.entityType = 'SONG' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM sections t LEFT JOIN sync_state s ON s.entityType = 'SECTION' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM verses t LEFT JOIN sync_state s ON s.entityType = 'VERSE' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM versions t LEFT JOIN sync_state s ON s.entityType = 'VERSION' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM ideas t LEFT JOIN sync_state s ON s.entityType = 'IDEA' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM folders t LEFT JOIN sync_state s ON s.entityType = 'FOLDER' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM genres t LEFT JOIN sync_state s ON s.entityType = 'GENRE' AND s.entityId = t.id
            WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM song_folders t LEFT JOIN sync_state s ON s.entityType = 'SONG_FOLDER'
            AND s.entityId = t.id WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM attachments t LEFT JOIN sync_state s ON s.entityType = 'ATTACHMENT'
            AND s.entityId = t.id WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM song_genres t LEFT JOIN sync_state s ON s.entityType = 'SONG_GENRE'
            AND s.entityId = t.id WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        + (SELECT COUNT(*) FROM favorites t LEFT JOIN sync_state s ON s.entityType = 'FAVORITE'
            AND s.entityId = t.songId WHERE s.entityId IS NULL OR t.updatedAt > s.syncedUpdatedAt)
        """,
    )
    fun observePendingCount(): Flow<Int>
}
