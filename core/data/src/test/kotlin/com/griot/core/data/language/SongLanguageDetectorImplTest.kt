package com.griot.core.data.language

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.griot.core.data.model.SongLanguage
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import java.util.concurrent.Executor
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class SongLanguageDetectorImplTest {
    private val identifier = mockk<LanguageIdentifier>()
    private val detector = SongLanguageDetectorImpl(identifier)

    @Nested
    inner class Detect {
        @ParameterizedTest
        @CsvSource("fr, FRENCH", "en, ENGLISH")
        fun `should read the dominant language`(tag: String, expected: SongLanguage) = runTest {
            // given
            val task = completedTask(successful = true, result = tag)
            every { identifier.identifyLanguage(any()) } returns task

            // when
            val language = detector.detect("my-text")

            // then
            language shouldBe expected
            verifySequence { identifier.identifyLanguage("my-text") }
        }

        @ParameterizedTest
        @ValueSource(strings = ["und", "es"])
        fun `should find nothing for another or an undetermined language`(tag: String) = runTest {
            // given
            val task = completedTask(successful = true, result = tag)
            every { identifier.identifyLanguage(any()) } returns task

            // when
            val language = detector.detect("my-text")

            // then
            language.shouldBeNull()
            verifySequence { identifier.identifyLanguage("my-text") }
        }

        @Test
        fun `should find nothing when the identification fails`() = runTest {
            // given
            val task = completedTask(successful = false, result = null)
            every { identifier.identifyLanguage(any()) } returns task

            // when
            val language = detector.detect("my-text")

            // then
            language.shouldBeNull()
            verifySequence { identifier.identifyLanguage("my-text") }
        }

        @Test
        fun `should not identify a blank text`() = runTest {
            // when
            val language = detector.detect("  ")

            // then
            language.shouldBeNull()
            verify { identifier wasNot Called }
        }
    }

    // A task already done, which calls back at once on the executor given.
    private fun completedTask(successful: Boolean, result: String?): Task<String> {
        val listener = slot<OnCompleteListener<String>>()
        val task = mockk<Task<String>>()
        every { task.isSuccessful } returns successful
        every { task.result } returns result
        every { task.addOnCompleteListener(any<Executor>(), capture(listener)) } answers {
            listener.captured.onComplete(task)
            task
        }
        return task
    }
}
