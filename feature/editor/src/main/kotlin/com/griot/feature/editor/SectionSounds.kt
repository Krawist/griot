package com.griot.feature.editor

/**
 * How the verses of a section sound.
 *
 * @property verses Each verse's sound, by verse id.
 * @property scheme Its rhyme scheme, one letter per verse that is not empty (« ABAB »).
 */
internal data class SectionSounds(val verses: Map<String, VerseSound> = emptyMap(), val scheme: String = "")
