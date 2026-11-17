package com.griot.feature.editor.audio

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class LinkServiceTest {
    @Nested
    inner class Of {
        @ParameterizedTest
        @CsvSource(
            "https://open.spotify.com/track/1, SPOTIFY",
            "https://youtu.be/abc, YOUTUBE",
            "https://m.youtube.com/watch?v=abc, YOUTUBE",
            "https://soundcloud.com/a/b, SOUNDCLOUD",
            "https://example.com/beat, OTHER",
        )
        fun `should find the service of a link from its host`(url: String, service: LinkService) {
            // when
            val found = LinkService.of(url)

            // then
            found shouldBe service
        }
    }

    @Nested
    inner class FormatDuration {
        @Test
        fun `should write minutes and seconds`() {
            // when
            val text = formatDuration(200_000)

            // then
            text shouldBe "3:20"
        }
    }
}
