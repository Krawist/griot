package com.griot.feature.editor.audio

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/**
 * A memo asked for.
 *
 * @property verseId The verse selected when it was asked; `null` without one.
 * @property onVerse Whether it is a memo on that verse (« Mémo sur ce vers ») rather than on its section.
 */
internal data class MemoRequest(val verseId: String? = null, val onVerse: Boolean = false) {
    companion object {
        /** Keeps the request across a configuration change, as `rememberSaveable` needs. */
        val Saver: Saver<MemoRequest?, Any> = listSaver(
            save = { request ->
                if (request ==
                    null
                ) {
                    emptyList()
                } else {
                    listOf(request.verseId.orEmpty(), request.onVerse)
                }
            },
            restore = { values ->
                if (values.isEmpty()) {
                    null
                } else {
                    MemoRequest(
                        verseId = (values[0] as String).ifEmpty {
                            null
                        },
                        onVerse = values[1] as Boolean,
                    )
                }
            },
        )
    }
}
