package com.griot.core.data.model

import java.time.Instant

/**
 * An idea of the ideas box.
 *
 * @property id UUID of the idea.
 * @property text The sentence or title; empty for an image alone.
 * @property imagePath Path of its image in the app's files; `null` for a text idea.
 * @property songId The song it is linked to; `null` for a free idea.
 * @property songTitle Title of that song, empty when it has none; `null` for a free idea.
 * @property createdAt When it was saved.
 */
data class Idea(
    val id: String,
    val text: String,
    val imagePath: String?,
    val songId: String?,
    val songTitle: String?,
    val createdAt: Instant,
)
