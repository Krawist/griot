package com.griot.feature.editor.audio

import com.griot.core.data.model.Attachment

/**
 * An audio of the song as its row shows it.
 *
 * @property attachment The audio.
 * @property missing Whether its file is on this phone but can no longer be read (moved or deleted).
 */
internal data class AudioItem(val attachment: Attachment, val missing: Boolean = false)
