package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import java.text.Normalizer
import java.util.Locale

private val Accents = Regex("\\p{Mn}+")

/**
 * Whether the song's title or verses contain [query], loosely: accents, case and apostrophes are ignored
 * (« neons » finds « néons »). A blank query matches every song.
 */
internal fun LibrarySong.matches(query: String): Boolean {
    val wanted = query.loose().trim()
    return wanted.isEmpty() || wanted in title.loose() || wanted in lyrics.loose()
}

private fun String.loose(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace(Accents, "")
    .replace('’', '\'')
    .replace("œ", "oe")
    .replace("æ", "ae")
    .lowercase(Locale.ROOT)
