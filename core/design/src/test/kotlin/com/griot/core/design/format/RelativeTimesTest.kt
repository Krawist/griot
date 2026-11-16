package com.griot.core.design.format

import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class RelativeTimesTest {
    // Douala: UTC+1, no daylight saving time.
    private val zone = ZoneId.of("Africa/Douala")

    @Nested
    inner class RelativeTimeOf {
        @ParameterizedTest
        @CsvSource("2026-11-16T15:59:30Z", "2026-11-16T16:05:00Z")
        fun `should read just now under a minute or ahead of the clock`(instant: String) {
            // when
            val time = relativeTime(Instant.parse(instant), now = Instant.parse("2026-11-16T16:00:00Z"), zone = zone)

            // then
            time shouldBe RelativeTime.JustNow
        }

        @ParameterizedTest
        @CsvSource("2026-11-16T15:59:00Z, 1", "2026-11-16T15:00:30Z, 59")
        fun `should count the minutes within the hour`(instant: String, minutes: Long) {
            // when
            val time = relativeTime(Instant.parse(instant), now = Instant.parse("2026-11-16T16:00:00Z"), zone = zone)

            // then
            time shouldBe RelativeTime.MinutesAgo(minutes)
        }

        @Test
        fun `should count the hours earlier today`() {
            // when
            val time = relativeTime(
                Instant.parse("2026-11-16T00:30:00Z"),
                now = Instant.parse("2026-11-16T16:00:00Z"),
                zone = zone,
            )

            // then
            time shouldBe RelativeTime.HoursAgo(15)
        }

        @Test
        fun `should read yesterday for the day before in the phone's time zone`() {
            // when
            // 22:30 UTC on the 15th is 23:30 on the 15th in Douala.
            val time = relativeTime(
                Instant.parse("2026-11-15T22:30:00Z"),
                now = Instant.parse("2026-11-16T16:00:00Z"),
                zone = zone,
            )

            // then
            time shouldBe RelativeTime.Yesterday
        }

        @Test
        fun `should give the day before yesterday`() {
            // when
            val time = relativeTime(
                Instant.parse("2026-11-12T10:00:00Z"),
                now = Instant.parse("2026-11-16T16:00:00Z"),
                zone = zone,
            )

            // then
            time shouldBe RelativeTime.Day(date = LocalDate.parse("2026-11-12"), thisYear = true)
        }

        @Test
        fun `should give the year of a day in another year`() {
            // when
            val time = relativeTime(
                Instant.parse("2025-12-24T10:00:00Z"),
                now = Instant.parse("2026-11-16T16:00:00Z"),
                zone = zone,
            )

            // then
            time shouldBe RelativeTime.Day(date = LocalDate.parse("2025-12-24"), thisYear = false)
        }
    }
}
