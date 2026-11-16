package com.griot.core.data.song

import com.griot.core.data.database.entity.SectionEntity
import com.griot.core.data.database.entity.VerseEntity
import com.griot.core.data.model.NewSection
import java.time.Instant
import java.util.UUID

/**
 * The rows of [sections] in the song [songId], dated [now], from position 0: a repeat points to the row of its
 * source and holds no verses. Inserted in order, a repeat's source always exists first.
 */
internal fun newSectionRows(
    songId: String,
    sections: List<NewSection>,
    now: Instant,
): Pair<List<SectionEntity>, List<VerseEntity>> {
    val ids = sections.map { UUID.randomUUID().toString() }
    val rows = sections.mapIndexed { position, section ->
        SectionEntity(
            id = ids[position],
            songId = songId,
            kind = section.kind,
            name = section.name,
            position = position,
            repeatOfSectionId = section.repeatOf?.let(ids::get),
            updatedAt = now,
        )
    }
    val verses = sections.flatMapIndexed { index, section ->
        section.lines.takeIf { section.repeatOf == null }.orEmpty().mapIndexed { position, text ->
            VerseEntity(
                id = UUID.randomUUID().toString(),
                sectionId = ids[index],
                text = text,
                position = position,
                updatedAt = now,
            )
        }
    }
    return rows to verses
}
