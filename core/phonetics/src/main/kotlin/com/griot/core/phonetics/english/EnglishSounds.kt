package com.griot.core.phonetics.english

// Each ARPAbet sound written in Lexique's alphabet, the one rhyme keys share: the French sound when French has the
// same or a close one, else an IPA letter Lexique never uses, so that it only rhymes in English. Two English sounds
// never share a writing, so that no English rhyme changes; only a diphthong and its vowel followed by its glide
// (« OY » and « AO Y »), which sound alike, end the same.
private val Sounds = mapOf(
    // Vowels.
    "AA" to "a",
    "AE" to "æ",
    "AH" to "ʌ",
    "AO" to "O",
    "AW" to "aw",
    "AY" to "aj",
    "EH" to "E",
    "ER" to "9R",
    "EY" to "e",
    "IH" to "ɪ",
    "IY" to "i",
    "OW" to "o",
    "OY" to "Oj",
    "UH" to "ʊ",
    "UW" to "u",
    // Consonants.
    "B" to "b",
    "CH" to "tS",
    "D" to "d",
    "DH" to "ð",
    "F" to "f",
    "G" to "g",
    "HH" to "h",
    "JH" to "dZ",
    "K" to "k",
    "L" to "l",
    "M" to "m",
    "N" to "n",
    "NG" to "G",
    "P" to "p",
    "R" to "R",
    "S" to "s",
    "SH" to "S",
    "T" to "t",
    "TH" to "θ",
    "V" to "v",
    "W" to "w",
    "Y" to "j",
    "Z" to "z",
    "ZH" to "Z",
)

/** The ARPAbet [sounds] (without stress) in the alphabet of the rhyme keys; an unknown sound is kept as it is. */
internal fun inLexique(sounds: List<String>): String = sounds.joinToString(separator = "") { Sounds[it] ?: it }
