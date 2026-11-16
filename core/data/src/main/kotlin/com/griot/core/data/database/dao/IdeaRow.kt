package com.griot.core.data.database.dao

import java.time.Instant

/**
 * An idea with the title of its song, read in one query.
 *
 * @property id UUID of the idea.
 * @property text The sentence or title; empty for an image alone.
 * @property imagePath Path of its image in the app's files; `null` for a text idea.
 * @property songId The song it is linked to; `null` for a free idea, or when the song went to the trash.
 * @property songTitle Title of that song; `null` without song.
 * @property createdAt When the idea was saved.
 */
internal data class IdeaRow(
    val id: String,
    val text: String,
    val imagePath: String?,
    val songId: String?,
    val songTitle: String?,
    val createdAt: Instant,
)
