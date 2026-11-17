package com.griot.core.data.audio

import android.graphics.Bitmap

/**
 * The cover of a beat and the colours of its player.
 *
 * @property image The file's embedded artwork; `null` without one: Griot then draws its own cover.
 * @property colors The player's colours, from the artwork, or Griot's own.
 */
data class CoverArt(val image: Bitmap?, val colors: PlayerColors)
