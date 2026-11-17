package com.griot.core.data.audio

import com.griot.core.data.database.dao.AttachmentDao
import com.griot.core.data.database.entity.AttachmentEntity
import com.griot.core.data.model.Attachment
import com.griot.core.data.model.AttachmentKind
import com.griot.core.data.preferences.UserPreferencesRepository
import java.net.URI
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Default implementation for [AttachmentRepository], on the Room database. */
@Singleton
internal class AttachmentRepositoryImpl @Inject constructor(
    private val attachmentDao: AttachmentDao,
    private val files: AudioFiles,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : AttachmentRepository {
    override fun observeAttachments(songId: String): Flow<List<Attachment>> =
        attachmentDao.observeBySong(songId).map { rows ->
            val device = preferences.deviceId()
            rows.map { it.toAttachment(device) }
        }

    override suspend fun addFile(songId: String, uri: String): String? {
        val info = files.take(uri) ?: return null
        val now = Instant.now(clock)
        val attachment = AttachmentEntity(
            songId = songId,
            kind = AttachmentKind.FILE,
            uri = uri,
            title = info.title,
            durationMillis = info.durationMillis,
            deviceId = preferences.deviceId(),
            createdAt = now,
            updatedAt = now,
        )
        attachmentDao.insert(attachment)
        return attachment.id
    }

    override suspend fun addLink(songId: String, url: String, title: String): String {
        val now = Instant.now(clock)
        val address = url.trim().let { if ("://" in it) it else "https://$it" }
        val attachment = AttachmentEntity(
            songId = songId,
            kind = AttachmentKind.LINK,
            uri = address,
            title = title.trim().ifBlank { hostOf(address) },
            createdAt = now,
            updatedAt = now,
        )
        attachmentDao.insert(attachment)
        return attachment.id
    }

    override suspend fun relink(id: String, uri: String): Boolean {
        if (files.take(uri) == null) return false
        attachmentDao.relink(id = id, uri = uri, deviceId = preferences.deviceId(), at = Instant.now(clock))
        return true
    }

    override suspend fun rename(id: String, title: String) {
        if (title.isNotBlank()) attachmentDao.rename(id = id, title = title.trim(), at = Instant.now(clock))
    }

    override suspend fun delete(id: String) {
        attachmentDao.markDeleted(id = id, at = Instant.now(clock))
    }

    // « open.spotify.com » for a link without a title.
    private fun hostOf(url: String): String = runCatching { URI(url).host }.getOrNull()?.removePrefix("www.") ?: url

    private fun AttachmentEntity.toAttachment(device: String) = Attachment(
        id = id,
        songId = songId,
        sectionId = sectionId,
        verseId = verseId,
        kind = kind,
        uri = uri,
        title = title,
        durationMillis = durationMillis,
        createdAt = createdAt,
        onThisPhone = kind != AttachmentKind.FILE || deviceId == device,
    )
}
