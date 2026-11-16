package com.griot.core.data.model

import java.time.Instant

/**
 * A past text of a verse.
 *
 * @property id UUID of the version.
 * @property text The verse as it was.
 * @property source Written by the author, accepted from the AI, restored, or kept from a sync conflict.
 * @property createdAt When it was recorded.
 */
data class VerseVersion(val id: String, val text: String, val source: VersionSource, val createdAt: Instant)
