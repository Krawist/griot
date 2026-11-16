package com.griot.core.design.format

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.griot.core.design.R
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** How [instant] reads at [now] in the time [zone]: minutes, hours today, yesterday, then the day. */
fun relativeTime(instant: Instant, now: Instant, zone: ZoneId): RelativeTime {
    val elapsed = Duration.between(instant, now)
    val day = instant.atZone(zone).toLocalDate()
    val today = now.atZone(zone).toLocalDate()
    return when {
        elapsed.toMinutes() < 1 -> RelativeTime.JustNow
        elapsed.toHours() < 1 -> RelativeTime.MinutesAgo(elapsed.toMinutes())
        day == today -> RelativeTime.HoursAgo(elapsed.toHours())
        day == today.minusDays(1) -> RelativeTime.Yesterday
        else -> RelativeTime.Day(date = day, thisYear = day.year == today.year)
    }
}

/** The text of this time in the interface language. */
@Composable
fun RelativeTime.text(): String = when (this) {
    RelativeTime.JustNow -> stringResource(R.string.time_just_now)
    is RelativeTime.MinutesAgo -> stringResource(R.string.time_minutes_ago, minutes)
    is RelativeTime.HoursAgo -> stringResource(R.string.time_hours_ago, hours)
    RelativeTime.Yesterday -> stringResource(R.string.time_yesterday)
    is RelativeTime.Day -> {
        val locale = LocalConfiguration.current.locales[0]
        val pattern = if (thisYear) "d MMM" else "d MMM yyyy"
        date.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}
