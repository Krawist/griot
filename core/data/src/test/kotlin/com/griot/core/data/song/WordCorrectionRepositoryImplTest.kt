package com.griot.core.data.song

import com.griot.core.data.database.inMemoryDatabase
import com.griot.core.data.model.SongLanguage
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

// On an in-memory database: a correction replaces the one before, as read back.
internal class WordCorrectionRepositoryImplTest {
    private val database = inMemoryDatabase()
    private val repository = WordCorrectionRepositoryImpl(
        dao = database.wordCorrectionDao(),
        clock = Clock.fixed(Instant.parse("2026-11-16T10:00:00Z"), ZoneOffset.UTC),
    )

    @AfterEach
    fun tearDown() {
        database.close()
    }

    @Nested
    inner class Correct {
        @Test
        fun `should keep the last count of a word, in lower case, for its language only`() = runTest {
            // given
            repository.correct(language = SongLanguage.FRENCH, word = "Bonabéri", syllables = 3)

            // when
            repository.correct(language = SongLanguage.FRENCH, word = "bonabéri", syllables = 4)

            // then
            repository.observeCorrections(SongLanguage.FRENCH).first() shouldBe mapOf("bonabéri" to 4)
            repository.observeCorrections(SongLanguage.ENGLISH).first() shouldBe emptyMap()
        }
    }
}
