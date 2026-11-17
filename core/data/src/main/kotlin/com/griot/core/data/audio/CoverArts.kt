package com.griot.core.data.audio

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

/** The [image] of a cover with the player's colours, from its swatches by AndroidX Palette. */
internal fun coverArtOf(image: Bitmap): CoverArt {
    val palette = Palette.from(image).generate()
    return CoverArt(
        image = image,
        colors = PlayerColors.from(
            dark = (palette.darkMutedSwatch ?: palette.darkVibrantSwatch ?: palette.dominantSwatch)?.rgb,
            light = (palette.vibrantSwatch ?: palette.lightVibrantSwatch)?.rgb,
            secondLight = (palette.lightMutedSwatch ?: palette.mutedSwatch)?.rgb,
        ),
    )
}
