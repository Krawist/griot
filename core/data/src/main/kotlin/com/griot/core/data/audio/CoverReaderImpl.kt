package com.griot.core.data.audio

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.palette.graphics.Palette
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Default implementation for [CoverReader]: the embedded picture, and its swatches by AndroidX Palette. */
@Singleton
internal class CoverReaderImpl @Inject constructor(@param:ApplicationContext private val context: Context) :
    CoverReader {
    override suspend fun read(uri: String): CoverArt = withContext(Dispatchers.IO) {
        val bytes = try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, Uri.parse(uri))
                retriever.embeddedPicture
            }
        } catch (_: RuntimeException) {
            null
        }
        val image = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            ?: return@withContext CoverArt(image = null, colors = PlayerColors.Griot)
        val palette = Palette.from(image).generate()
        CoverArt(
            image = image,
            colors = PlayerColors.from(
                dark = (palette.darkMutedSwatch ?: palette.darkVibrantSwatch ?: palette.dominantSwatch)?.rgb,
                light = (palette.vibrantSwatch ?: palette.lightVibrantSwatch)?.rgb,
                secondLight = (palette.lightMutedSwatch ?: palette.mutedSwatch)?.rgb,
            ),
        )
    }
}
