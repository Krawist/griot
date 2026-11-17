package com.griot.core.data.audio

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Default implementation for [AudioFiles], through the content resolver, off the main thread. */
@Singleton
internal class AudioFilesImpl @Inject constructor(@param:ApplicationContext private val context: Context) : AudioFiles {
    override suspend fun take(uri: String): AudioFileInfo? = withContext(Dispatchers.IO) {
        val address = Uri.parse(uri)
        try {
            // The right to read it stays after a restart: the file is never copied.
            context.contentResolver.takePersistableUriPermission(address, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            AudioFileInfo(title = nameOf(address), durationMillis = durationOf(address))
        } catch (_: SecurityException) {
            null
        }
    }

    override suspend fun isAvailable(uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openFileDescriptor(Uri.parse(uri), "r")?.use { true } ?: false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    // The file's name without its extension (« Beat afro 94 BPM.mp3 » → « Beat afro 94 BPM »).
    private fun nameOf(uri: Uri): String {
        val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: uri.lastPathSegment.orEmpty()
        return name.substringBeforeLast('.').ifBlank { name }
    }

    private fun durationOf(uri: Uri): Long? = try {
        MediaMetadataRetriever().use { retriever ->
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        }
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: RuntimeException) {
        null
    }
}
