package com.griot.core.data.idea

import android.content.Context
import android.net.Uri
import com.griot.core.data.database.dao.IdeaDao
import com.griot.core.data.database.dao.IdeaRow
import com.griot.core.data.database.entity.IdeaEntity
import com.griot.core.data.model.Idea
import com.griot.core.data.song.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Default implementation for [IdeaRepository], on the Room database; images are copied to `files/ideas/`. */
// One function per change of the ideas box.
@Suppress("TooManyFunctions")
@Singleton
internal class IdeaRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val ideaDao: IdeaDao,
    private val songs: SongRepository,
    private val clock: Clock,
) : IdeaRepository {
    override fun observeIdeas(): Flow<List<Idea>> = ideaDao.observeWithSongs().map { rows -> rows.map { it.toIdea() } }

    override fun observeSongIdeas(songId: String): Flow<List<Idea>> =
        observeIdeas().map { ideas -> ideas.filter { it.songId == songId } }

    override suspend fun addIdea(text: String): String = insert(text = text.trim(), imagePath = null)

    override suspend fun addImageIdea(uri: String): String? = withContext(Dispatchers.IO) {
        val folder = File(context.filesDir, IMAGES).apply { mkdirs() }
        val file = File(folder, "${UUID.randomUUID()}.jpg")
        val copied = try {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                file.outputStream().use { input.copyTo(it) }
            } != null
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
        if (copied) insert(text = "", imagePath = file.absolutePath) else null
    }

    override suspend fun updateIdea(id: String, text: String) {
        ideaDao.updateText(id = id, text = text.trim(), at = Instant.now(clock))
    }

    override suspend fun linkIdea(id: String, songId: String?) {
        ideaDao.link(id = id, songId = songId, at = Instant.now(clock))
    }

    override suspend fun startSong(id: String): String {
        val idea = requireNotNull(ideaDao.getById(id)) { "No idea $id" }
        val songId = songs.createSong(firstVerse = idea.text)
        linkIdea(id = id, songId = songId)
        return songId
    }

    override suspend fun deleteIdea(id: String) {
        ideaDao.markDeleted(id = id, at = Instant.now(clock))
    }

    override suspend fun restoreIdea(id: String) {
        ideaDao.restore(id = id, at = Instant.now(clock))
    }

    private suspend fun insert(text: String, imagePath: String?): String {
        val now = Instant.now(clock)
        val idea = IdeaEntity(
            id = UUID.randomUUID().toString(),
            text = text,
            imagePath = imagePath,
            createdAt = now,
            updatedAt = now,
        )
        ideaDao.insert(idea)
        return idea.id
    }

    private fun IdeaRow.toIdea() = Idea(
        id = id,
        text = text,
        imagePath = imagePath,
        songId = songId,
        songTitle = songTitle,
        createdAt = createdAt,
    )

    private companion object {
        const val IMAGES = "ideas"
    }
}
