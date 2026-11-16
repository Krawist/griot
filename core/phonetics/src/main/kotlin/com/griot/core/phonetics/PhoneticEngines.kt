package com.griot.core.phonetics

import com.griot.core.phonetics.english.EnglishEngine
import com.griot.core.phonetics.french.FrenchEngine

/** The engines of every language. Make one for the app: the dictionaries are read on first use, then kept. */
class PhoneticEngines {
    private val frenchDictionary = PhoneticDictionary("/phonetics/fr-lexique.tsv.gz")
    private val englishDictionary = PhoneticDictionary("/phonetics/en-cmudict.tsv.gz")

    /** The French engine, on Lexique. */
    val french: PhoneticEngine = FrenchEngine(dictionary = frenchDictionary, other = { null })

    /** The English engine, on the CMU Pronouncing Dictionary. */
    val english: PhoneticEngine = EnglishEngine(dictionary = englishDictionary, other = { null })

    /** The engine of [language]. */
    fun of(language: PhoneticLanguage): PhoneticEngine = when (language) {
        PhoneticLanguage.FRENCH -> french
        PhoneticLanguage.ENGLISH -> english
    }
}
