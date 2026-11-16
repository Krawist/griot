package com.griot.core.data.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.griot.core.data.model.AppLanguage
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verifySequence
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class AppLanguageRepositoryImplTest {
    private val repository = AppLanguageRepositoryImpl()

    @BeforeEach
    fun setUp() {
        mockkStatic(AppCompatDelegate::class, LocaleListCompat::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Nested
    inner class Current {
        @ParameterizedTest
        @CsvSource("fr, FRENCH", "en, ENGLISH", "'', SYSTEM")
        fun `should read the language of the interface`(tags: String, expected: AppLanguage) {
            // given
            val locales = mockk<LocaleListCompat> { every { toLanguageTags() } returns tags }
            every { AppCompatDelegate.getApplicationLocales() } returns locales

            // when
            val language = repository.current()

            // then
            language shouldBe expected
            verifySequence {
                AppCompatDelegate.getApplicationLocales()
                locales.toLanguageTags()
            }
        }
    }

    @Nested
    inner class Set {
        @ParameterizedTest
        @CsvSource("FRENCH, fr", "ENGLISH, en", "SYSTEM, ''")
        fun `should apply the language to the interface`(language: AppLanguage, tags: String) {
            // given
            val locales = mockk<LocaleListCompat>()
            every { LocaleListCompat.forLanguageTags(any()) } returns locales
            justRun { AppCompatDelegate.setApplicationLocales(any()) }

            // when
            repository.set(language)

            // then
            verifySequence {
                LocaleListCompat.forLanguageTags(tags)
                AppCompatDelegate.setApplicationLocales(locales)
            }
        }
    }
}
