package com.griot.core.data.database.entity

import androidx.room.Entity
import com.griot.core.data.model.SongLanguage
import java.time.Instant

/**
 * The syllables the user gave to a word the dictionaries do not know (« Bonabéri » → 4), used in every song.
 *
 * @property language Language of the word: French or English.
 * @property word The word, in lower case.
 * @property syllables Its syllables, as sung.
 * @property updatedAt Last change of the correction, on any device; sync keeps the most recent.
 */
@Entity(tableName = "word_corrections", primaryKeys = ["language", "word"])
internal data class WordCorrectionEntity(
    val language: SongLanguage,
    val word: String,
    val syllables: Int,
    val updatedAt: Instant,
)
