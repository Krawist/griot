package com.griot.feature.library

import com.griot.core.data.model.LibrarySong
import java.time.Duration
import java.time.Instant

/** The id of « Récents », the folder of the songs changed in the last 24 hours: always there, never stored. */
internal const val RECENT_FOLDER_ID = "recent"

private val RecentWindow = Duration.ofHours(24)

/** Whether the song changed in the 24 hours before [now]: a rolling window, so a song changed 25 hours ago is not. */
internal fun LibrarySong.isRecent(now: Instant): Boolean = Duration.between(updatedAt, now) < RecentWindow
