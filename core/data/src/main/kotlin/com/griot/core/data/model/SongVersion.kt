package com.griot.core.data.model

import java.time.Instant

/**
 * A past content of a whole song.
 *
 * @property id UUID of the version.
 * @property snapshot The song as it was.
 * @property source Written by the author, restored, or kept from a sync conflict.
 * @property createdAt When it was recorded.
 */
data class SongVersion(val id: String, val snapshot: SongSnapshot, val source: VersionSource, val createdAt: Instant)
