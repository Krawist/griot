package com.griot.core.design.format

import java.time.LocalDate

/** When something happened, as the user reads it: « à l'instant », « il y a 5 min », « hier », « 12 nov. ». */
sealed interface RelativeTime {
    /** Less than a minute ago, or a time ahead of the phone's clock (another device's). */
    data object JustNow : RelativeTime

    /**
     * Less than an hour ago.
     *
     * @property minutes Whole minutes elapsed, from 1 to 59.
     */
    data class MinutesAgo(val minutes: Long) : RelativeTime

    /**
     * Earlier today.
     *
     * @property hours Whole hours elapsed, from 1.
     */
    data class HoursAgo(val hours: Long) : RelativeTime

    /** The day before today. */
    data object Yesterday : RelativeTime

    /**
     * Before yesterday.
     *
     * @property date The day, in the phone's time zone.
     * @property thisYear Whether it is in the current year: the year is then left out.
     */
    data class Day(val date: LocalDate, val thisYear: Boolean) : RelativeTime
}
