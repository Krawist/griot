package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class SongChecksTest {
    @Nested
    inner class CheckTitle {
        @Test
        fun `should keep a short title without its label and final dot`() {
            // when
            val title = checkTitle("Title: Ce soir encore.")

            // then
            title shouldBe "Ce soir encore"
        }

        @Test
        fun `should drop a sentence`() {
            // when
            val title = checkTitle("Here is a title that I would suggest for this song")

            // then
            title.shouldBeNull()
        }
    }

    @Nested
    inner class CheckClassification {
        @Test
        fun `should keep the folder and the genre among those given`() {
            // given
            val request = SongRequest(
                language = PhoneticLanguage.FRENCH,
                lyrics = listOf("my-verse"),
                folders = listOf("EP 2027"),
                genres = listOf("Afro", "Rap"),
            )

            // when
            val classification = checkClassification(answer = "Folder: ep 2027\nGenre: Afro.", request = request)

            // then
            classification shouldBe Classification(folder = "EP 2027", genre = "Afro")
        }

        @Test
        fun `should drop names that were not given`() {
            // given
            val request = SongRequest(
                language = PhoneticLanguage.FRENCH,
                lyrics = listOf("my-verse"),
                folders = listOf("EP 2027"),
                genres = listOf("Afro"),
            )

            // when
            val classification = checkClassification(answer = "Folder: Love songs\nGenre: Jazz", request = request)

            // then
            classification.shouldBeNull()
        }
    }
}
