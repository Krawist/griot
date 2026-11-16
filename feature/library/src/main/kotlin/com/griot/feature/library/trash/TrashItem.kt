package com.griot.feature.library.trash

import com.griot.core.data.model.TrashedSong
import com.griot.core.design.format.RelativeTime

/**
 * One song of the trash.
 *
 * @property song The song deleted.
 * @property deleted When it was deleted, as the row reads it.
 * @property daysLeft Days before it is removed for good, at least 1.
 */
internal data class TrashItem(val song: TrashedSong, val deleted: RelativeTime, val daysLeft: Long)
