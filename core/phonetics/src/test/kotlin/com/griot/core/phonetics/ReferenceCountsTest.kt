package com.griot.core.phonetics

import io.kotest.assertions.withClue
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

// Verses counted by hand by the conventions of songwriting, each language with its own set.
internal class ReferenceCountsTest {
    private val engines = PhoneticEngines()

    @ParameterizedTest
    @CsvSource("FRENCH, /reference/fr-verses.tsv", "ENGLISH, /reference/en-verses.tsv")
    fun `should count at least 95 % of the reference verses exactly`(language: PhoneticLanguage, resource: String) {
        // given
        val verses = requireNotNull(javaClass.getResourceAsStream(resource)).bufferedReader().readLines()
            .filter { it.isNotBlank() }
            .map { line -> line.substringBefore('\t') to line.substringAfter('\t').toInt() }
        val engine = engines.of(language)

        // when
        val misses = verses.mapNotNull { (verse, expected) ->
            val counted = engine.countSyllables(verse).syllables
            "$verse: $counted instead of $expected".takeIf { counted != expected }
        }

        // then
        withClue(misses.joinToString(separator = "\n")) {
            (1.0 - misses.size.toDouble() / verses.size) shouldBeGreaterThanOrEqual 0.95
        }
    }
}
