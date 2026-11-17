package com.griot.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.griot.core.data.database.entity.AttachmentEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
internal interface AttachmentDao {
    @Insert
    suspend fun insert(attachment: AttachmentEntity)

    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?

    /** The audios of the song [songId] that are not removed, newest first. */
    @Query("SELECT * FROM attachments WHERE songId = :songId AND deleted = 0 ORDER BY createdAt DESC")
    fun observeBySong(songId: String): Flow<List<AttachmentEntity>>

    /** Gives the file [id] its new address [uri] on this phone [deviceId], at [at]. */
    @Query("UPDATE attachments SET uri = :uri, deviceId = :deviceId, updatedAt = :at WHERE id = :id")
    suspend fun relink(id: String, uri: String, deviceId: String, at: Instant)

    /** Makes the file [id] the beat of the song [songId] at [at], and the song's other files not. */
    @Query(
        """
        UPDATE attachments SET beat = (id = :id), updatedAt = :at
        WHERE songId = :songId AND (beat = 1 OR id = :id)
        """,
    )
    suspend fun setBeat(songId: String, id: String, at: Instant)

    @Query("UPDATE attachments SET title = :title, updatedAt = :at WHERE id = :id")
    suspend fun rename(id: String, title: String, at: Instant)

    @Query("UPDATE attachments SET deleted = 1, updatedAt = :at WHERE id = :id")
    suspend fun markDeleted(id: String, at: Instant)
}
