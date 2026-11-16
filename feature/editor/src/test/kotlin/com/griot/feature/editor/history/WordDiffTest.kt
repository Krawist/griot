package com.griot.feature.editor.history

import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class WordDiffTest {
    @Nested
    inner class ChangedWords {
        @Test
        fun `should mark the word that changed`() {
            // when
            val segments = changedWords("Les néons pleurent sur le bitume", "Les néons saignent sur le bitume")

            // then
            segments shouldContainExactly listOf(
                DiffSegment(text = "Les néons ", changed = false),
                DiffSegment(text = "pleurent", changed = true),
                DiffSegment(text = " sur le bitume", changed = false),
            )
        }

        @Test
        fun `should mark several words in a row with the spaces between them`() {
            // when
            val segments = changedWords("La ville dort sous le bitume", "Les néons pleurent sur le bitume")

            // then
            segments shouldContainExactly listOf(
                DiffSegment(text = "La ville dort sous", changed = true),
                DiffSegment(text = " le bitume", changed = false),
            )
        }

        @Test
        fun `should mark nothing in the same text, case aside`() {
            // when
            val segments = changedWords("Et je chante", "et je chante")

            // then
            segments shouldContainExactly listOf(DiffSegment(text = "Et je chante", changed = false))
        }
    }
}
