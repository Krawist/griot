package com.griot.feature.editor

import com.griot.core.data.model.SongDocument

/** Every verse of the song, its repeats once, one per line: what its language is detected from. */
internal fun SongDocument.lyrics(): String =
    sections.filter { it.repeatOf == null }.flatMap { it.verses }.joinToString(separator = "\n") { it.text }
