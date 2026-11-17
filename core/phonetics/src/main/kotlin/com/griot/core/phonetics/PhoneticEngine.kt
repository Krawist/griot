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

    /**
     * Words that rhyme with [verse], from the dictionary of the language of its last word: by number of syllables,
     * the most common first, the verse's own last word left out. Empty when that word is in no dictionary.
     */
    fun rhymingWords(verse: String): List<RhymingWord>

    /**
     * Replaces the syllables the user gave to words of this language, by word in lower case: they win over the
     * dictionary and the grapheme rules, and the words are no longer guessed.
     */
    fun setCorrections(corrections: Map<String, Int>)
}
