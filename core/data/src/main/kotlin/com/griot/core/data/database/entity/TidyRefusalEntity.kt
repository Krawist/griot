package com.griot.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.griot.core.data.model.TidyUpKind
import java.time.Instant

/**
 * A tidy-up proposal the author refused: it is never proposed again. Kept on this phone only.
 *
 * @property id `kind:key`.
 * @property kind The kind of proposal.
 * @property key What it was about: the song's id, or the sorted ids of the duplicates.
 * @property createdAt When it was refused.
 */
@Entity(tableName = "tidy_refusals")
internal data class TidyRefusalEntity(
    @PrimaryKey val id: String,
    val kind: TidyUpKind,
    val key: String,
    val createdAt: Instant,
)
