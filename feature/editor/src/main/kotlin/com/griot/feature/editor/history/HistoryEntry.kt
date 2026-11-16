package com.griot.feature.editor.history

import com.griot.core.data.model.VersionSource
import com.griot.core.design.format.RelativeTime
import java.time.LocalTime

/**
 * One version on the history's timeline.
 *
 * @property versionId UUID of the version; `null` for the text as it is now, at the top.
 * @property recorded When it was recorded, as the timeline reads it; `null` for the text as it is now.
 * @property time Time of day it was recorded, shown after an old day; `null` for the text as it is now.
 * @property source What made it: the author, the AI, a restore or another device; `null` for the text as it is now.
 * @property lines Its lines, the words changed from its neighbour marked.
 */
internal data class HistoryEntry(
    val versionId: String?,
    val recorded: RelativeTime?,
    val time: LocalTime?,
    val source: VersionSource?,
    val lines: List<HistoryLine>,
)
