package com.griot.core.phonetics

/**
 * The phonetics of one language: how its words sound, how many syllables a verse has, and how a verse rhymes. The
 * editor and the AI only depend on this interface. Results are cached per word; the first call loads the dictionary,
 * so call it off the main thread.
 */
interface PhoneticEngine {
    /** The language of this engine. */
    val language: PhoneticLanguage

    /** The word as its dictionary gives it; `null` when it is not there. */
    fun lookup(word: String): Transcription?

    /** The word as its dictionary gives it, else from the other language's dictionary, else from grapheme rules. */
    fun transcribe(word: String): Transcription

    /** The syllables of [verse] as sung, by the conventions of songwriting in this language. */
    fun countSyllables(verse: String): SyllableCount

    /** The rhyme of [verse]; `null` for a verse without a word. */
    fun rhyme(verse: String): Rhyme?
}
