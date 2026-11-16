package com.griot.core.data.export

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import com.griot.core.data.R
import com.griot.core.data.model.ExportFile
import com.griot.core.data.model.SongDocument
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongEditorRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.sectionLabels
import com.griot.core.data.song.words
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/** Default implementation for [MarkdownExporter]: the files go to the app's cache, under `exports/`. */
@Singleton
internal class MarkdownExporterImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val songs: SongRepository,
    private val editor: SongEditorRepository,
    private val preferences: UserPreferencesRepository,
    private val clock: Clock,
) : MarkdownExporter {
    override suspend fun exportSong(songId: String): ExportFile? = withContext(Dispatchers.IO) {
        val document = editor.observeDocument(songId).first() ?: return@withContext null
        val resources = resources()
        val name =
            fileName(title = document.title, fallback = resources.getString(R.string.song_untitled), extension = "md")
        val file = File(folder(), name).apply { writeText(markdown(document, resources)) }
        ExportFile(name = name, mimeType = MARKDOWN, path = file.absolutePath)
    }

    override suspend fun exportAll(): ExportFile = withContext(Dispatchers.IO) {
        val resources = resources()
        val documents = songs.observeLibrary().first().mapNotNull { editor.observeDocument(it.id).first() }
        val untitled = resources.getString(R.string.song_untitled)
        val names = uniqueNames(documents.map { fileName(title = it.title, fallback = untitled, extension = "md") })
        val name = "Griot-${LocalDate.now(clock)}.zip"
        val file = File(folder(), name)
        ZipOutputStream(file.outputStream()).use { zip ->
            documents.zip(names).forEach { (document, entry) ->
                zip.putNextEntry(ZipEntry(entry))
                zip.write(markdown(document, resources).toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        ExportFile(name = name, mimeType = ZIP, path = file.absolutePath)
    }

    override suspend fun save(file: ExportFile, uri: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(Uri.parse(uri))?.use { output ->
                File(file.path).inputStream().use { it.copyTo(output) }
            } != null
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private suspend fun markdown(document: SongDocument, resources: Resources): String {
        val labels = sectionLabels(document.sections).map { it.words(resources) }
        return songMarkdown(
            title = document.title.ifBlank { resources.getString(R.string.song_untitled) },
            author = preferences.authorName.first(),
            sections = document.sections,
            labels = labels,
        )
    }

    // The app's resources in the language chosen for the interface, which the app's context alone may not have.
    private fun resources(): Resources {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (locales.isEmpty) return context.resources
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
        return context.createConfigurationContext(configuration).resources
    }

    // A fresh folder each time, so old exports do not pile up.
    private fun folder(): File = File(context.cacheDir, EXPORTS).apply {
        deleteRecursively()
        mkdirs()
    }

    private companion object {
        const val EXPORTS = "exports"
        const val MARKDOWN = "text/markdown"
        const val ZIP = "application/zip"
    }
}
