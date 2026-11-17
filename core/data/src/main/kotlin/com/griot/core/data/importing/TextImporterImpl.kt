package com.griot.core.data.importing

import com.griot.core.data.model.ImportResult
import com.griot.core.data.model.ImportedText
import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.toNewSections
import com.griot.core.phonetics.paste.PastedBlock
import com.griot.core.phonetics.paste.splitPastedText
import javax.inject.Inject
import javax.inject.Singleton

/** Default implementation for [TextImporter], splitting each text with the paste splitter. */
@Singleton
internal class TextImporterImpl @Inject constructor(
    private val songs: SongRepository,
    private val preferences: UserPreferencesRepository,
) : TextImporter {
    override suspend fun import(texts: List<ImportedText>): ImportResult {
        var created = 0
        var sections = 0
        val failed = mutableListOf<String>()
        texts.forEach { text ->
            val blocks = text.text?.let(::splitPastedText).orEmpty().withoutTitle(text.name)
            if (blocks.isEmpty()) {
                failed += text.name
            } else {
                songs.importSong(title = text.name, sections = blocks.toNewSections())
                created++
                sections += blocks.size
            }
        }
        // New songs: a tidy-up is proposed (GRI-59).
        if (created > 0) preferences.requestTidyUp()
        return ImportResult(songs = created, sections = sections, failed = failed)
    }

    // A note starts with its title, already the song's name (« Saison sèche »): it is not a verse. The repeats then
    // point one block earlier.
    private fun List<PastedBlock>.withoutTitle(name: String): List<PastedBlock> {
        val first = firstOrNull() ?: return this
        val title = first.kind == null && first.lines.singleOrNull()?.trim().equals(name.trim(), ignoreCase = true)
        return if (title && name.isNotBlank()) drop(1).map { it.copy(repeatOf = it.repeatOf?.minus(1)) } else this
    }
}
