package com.griot.core.data.song

import com.griot.core.data.model.NewSection
import com.griot.core.data.model.PastedSections
import com.griot.core.data.model.SectionKind
import com.griot.core.data.model.SongDocument
import com.griot.core.data.model.SongLanguage
import kotlinx.coroutines.flow.Flow

/** The content of one song, as the editor changes it. Every change is saved at once and dates the song. */
// One function per change the editor makes.
@Suppress("TooManyFunctions")
interface SongEditorRepository {
    /** The song [songId] with its sections and verses; `null` when it does not exist or is in the trash. */
    fun observeDocument(songId: String): Flow<SongDocument?>

    /**
     * Sets the writing [language] of the song [songId], [detected] from its lyrics (automatic mode) or chosen by hand.
     */
    suspend fun setLanguage(songId: String, language: SongLanguage, detected: Boolean)

    /** Adds a section of [kind] at the end of the song [songId], holding one empty verse, and returns its id. */
    suspend fun addSection(songId: String, kind: SectionKind): String

    /**
     * Adds at the end of the song a repeat of the section [sectionId] (of its source, for a repeat), which shows
     * the same verses without copying them, and returns its id.
     */
    suspend fun repeatSection(sectionId: String): String

    /** Names the section [sectionId]; a blank or `null` [name] gives it back its automatic label. */
    suspend fun renameSection(sectionId: String, name: String?)

    /** Moves the section [sectionId] by [offset] places (-1 up, 1 down), within the song. */
    suspend fun moveSection(sectionId: String, offset: Int)

    /** Deletes the section [sectionId] and its repeats. */
    suspend fun deleteSection(sectionId: String)

    /**
     * Merges the section [sectionId] into the one above it: its verses go to the end of that section, its repeats now
     * repeat that section, and it is deleted. Nothing happens for the first section, or when one of the two is a
     * repeat.
     */
    suspend fun mergeSection(sectionId: String)

    /**
     * Adds [text] as a new verse at the end of the song [songId], in its last section with verses of its own (a new
     * verse section when it has none).
     */
    suspend fun appendVerse(songId: String, text: String)

    /** Saves the [text] of the verse [verseId]. */
    suspend fun updateVerse(verseId: String, text: String)

    /**
     * Splits the verse [verseId] into [lines]: the verse keeps the first one, each other one becomes a new verse
     * below it, in order. Returns the ids of the new verses.
     */
    suspend fun splitVerse(verseId: String, lines: List<String>): List<String>

    /** Appends the text of the verse [verseId] to the verse [previousId], and deletes the verse [verseId]. */
    suspend fun mergeVerses(previousId: String, verseId: String)

    /**
     * Closes the section of the empty verse [verseId], which is deleted, and starts a new verse section right below,
     * holding one empty verse. Returns the id of that verse.
     */
    suspend fun closeSection(verseId: String): String

    /**
     * Pastes [sections] into the verse [verseId]: the first one's lines go into its section, as by [splitVerse]; the
     * others become new sections right after it, in order, a repeat pointing to its source (index 0 being the section
     * pasted into). Returns what was added, for [undoPaste].
     */
    suspend fun pasteSections(verseId: String, sections: List<NewSection>): PastedSections

    /** Replaces the whole content of the song [songId] by [sections]: its sections before are deleted. */
    suspend fun replaceContent(songId: String, sections: List<NewSection>)

    /** Removes what the paste added, then puts [lines] as verses into the verse [verseId] and below it. */
    suspend fun undoPaste(paste: PastedSections, verseId: String, lines: List<String>)
}
