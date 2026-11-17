package com.griot.core.phonetics

import com.griot.core.phonetics.english.EnglishEngine
import com.griot.core.phonetics.french.FrenchEngine

/**
 * The engines of every language, each one reading the other's words (an English word in a French song). Make one
 * for the app: the dictionaries are read on first use, then kept.
 */
class PhoneticEngines {
    private val frenchDictionary = PhoneticDictionary("/phonetics/fr-lexique.tsv.gz")
    private val englishDictionary = PhoneticDictionary("/phonetics/en-cmudict.tsv.gz")

    /** The French engine, on Lexique. */
    val french: PhoneticEngine = FrenchEngine(
        dictionary = frenchDictionary,
        ranks = WordRanks("/phonetics/fr-frequent.txt.gz"),
        other = { english },
    )

    /** The English engine, on the CMU Pronouncing Dictionary. */
    val english: PhoneticEngine = EnglishEngine(
        dictionary = englishDictionary,
        ranks = WordRanks("/phonetics/en-frequent.txt.gz"),
        other = { french },
    )

    /** The engine of [language]. */
    fun of(language: PhoneticLanguage): PhoneticEngine = when (language) {
        PhoneticLanguage.FRENCH -> french
        PhoneticLanguage.ENGLISH -> english
    }
}
