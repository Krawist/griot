package com.griot.feature.editor

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class WordInsertionTest {
    @Nested
    inner class InsertWord {
        @ParameterizedTest
        @CsvSource(
            delimiter = '|',
            value = [
                // At the end of the verse, after a word.
                "Le soleil cogne sur la|23|Le soleil cogne sur la tôle|27",
                // After a space.
                "Le soleil cogne sur la |24|Le soleil cogne sur la tôle|27",
                // Before a word.
                "Le soleil la|10|Le soleil tôle la|14",
                // After an apostrophe.
                "Le soleil sur l'|16|Le soleil sur l'tôle|20",
                // In an empty verse.
                "''|0|tôle|4",
                // A cursor beyond the text goes to its end.
                "sur la|99|sur la tôle|11",
            ],
        )
        fun `should insert the word at the cursor with the spaces it needs`(
            text: String,
            cursor: Int,
            expected: String,
            expectedCursor: Int,
        ) {
            // when
            val inserted = insertWord(text = text, cursor = cursor, word = "tôle")

            // then
            inserted shouldBe (expected to expectedCursor)
        }
    }
}
