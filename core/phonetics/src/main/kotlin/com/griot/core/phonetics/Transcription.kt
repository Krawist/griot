package com.griot.core.phonetics

/**
 * How a word sounds.
 *
 * @property phonemes The phonemes, in the notation of the engine's dictionary: Lexique's for French (one character
 * each), ARPAbet with stress for English (« AY1 »).
 * @property language The engine that transcribed it: a word of the other language is transcribed by its own engine.
 * @property known Whether the word is in a dictionary; `false` for a guess from grapheme rules.
 */
data class Transcription(val phonemes: List<String>, val language: PhoneticLanguage, val known: Boolean)
