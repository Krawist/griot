package com.griot.core.data.model

import java.time.Instant

/**
 * An audio of a song.
 *
 * @property id Stable UUID, the same on every device.
 * @property songId The song it belongs to.
 * @property sectionId The section it is about; `null` for the whole song.
 * @property verseId The verse it is about; `null` for a section or the whole song.
 * @property kind A memo, a file or a link.
 * @property uri The file's content address, or the link.
 * @property title What it is called.
 * @property durationMillis Its length in milliseconds; `null` when unknown (a link).
 * @property createdAt When it was added.
 * @property onThisPhone Whether its file is on this phone; `false` for a file added on another phone, which only syncs
 * as a reference. Always `true` for a link.
 */
data class Attachment(
    val id: String,
    val songId: String,
    val sectionId: String? = null,
    val verseId: String? = null,
    val kind: AttachmentKind,
    val uri: String,
    val title: String,
    val durationMillis: Long? = null,
    val createdAt: Instant,
    val onThisPhone: Boolean = true,
)
