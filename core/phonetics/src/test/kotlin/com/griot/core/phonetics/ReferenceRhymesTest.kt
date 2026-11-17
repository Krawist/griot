package com.griot.core.phonetics

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeEmpty
import org.junit.jupiter.api.Test

// French and English verses heard by hand: whether their endings rhyme, closely or exactly.
internal class ReferenceRhymesTest {
    private val engines = PhoneticEngines()

    @Test
    fun `should rhyme a French and an English verse as they are heard`() {
        // given
        val pairs = requireNotNull(javaClass.getResourceAsStream("/reference/cross-rhymes.tsv")).bufferedReader()
            .readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { it.split('\t') }

        // when
        val misses = pairs.mapNotNull { (french, english, expected) ->
            val rhymes = engines.french.rhyme(french)?.key == engines.english.rhyme(english)?.key
            "$french / $english: $rhymes instead of $expected".takeIf { rhymes != expected.toBoolean() }
        }

        // then
        withClue(misses.joinToString(separator = "\n")) { misses.shouldBeEmpty() }
    }
}
