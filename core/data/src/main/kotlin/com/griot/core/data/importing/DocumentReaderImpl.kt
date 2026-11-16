package com.griot.core.data.importing

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.griot.core.data.model.ImportedText
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Default implementation for [DocumentReader], through the content resolver, off the main thread. */
@Singleton
internal class DocumentReaderImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    DocumentReader {
    override suspend fun read(uris: List<String>): List<ImportedText> = withContext(Dispatchers.IO) {
        uris.map { uri -> Uri.parse(uri).let { ImportedText(name = nameOf(it), text = textOf(it)) } }
    }

    // The file's name without its extension (« Les néons.txt » → « Les néons »).
    private fun nameOf(uri: Uri): String {
        val name = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            ?: uri.lastPathSegment.orEmpty()
        return name.substringBeforeLast('.').ifBlank { name }
    }

    // The text as UTF-8; `null` when the file cannot be opened.
    private fun textOf(uri: Uri): String? = try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }
}
