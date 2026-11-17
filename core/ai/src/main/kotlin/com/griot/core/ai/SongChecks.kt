package com.griot.core.ai

/** At most this many words in a title proposed. */
private const val MAX_TITLE_WORDS = 6

/** The title proposed when it is a short title (1 to 6 words, not a sentence); `null` otherwise. */
internal fun checkTitle(answer: String?): String? {
    val title = answer?.removePrefix("Title:")?.removePrefix("Titre :")?.trim()?.trimEnd('.')?.trim().orEmpty()
    val words = title.split(Regex("""\s+""")).filter { it.isNotBlank() }
    return title.takeIf { words.size in 1..MAX_TITLE_WORDS }
}

/**
 * The folder and the genre of a model's [answer] (« Folder: … » and « Genre: … » lines), kept only when they are
 * among the names of the [request]; `null` when neither is.
 */
internal fun checkClassification(answer: String, request: SongRequest): Classification? {
    fun pick(label: String, names: List<String>): String? {
        val value = answer.lines().firstOrNull { it.trim().startsWith(label, ignoreCase = true) }
            ?.substringAfter(':')?.trim()?.trim('"', '«', '»', '.')?.trim()
            ?: return null
        return names.firstOrNull { it.equals(value, ignoreCase = true) }
    }
    val classification = Classification(folder = pick("Folder", request.folders), genre = pick("Genre", request.genres))
    return classification.takeIf { it.folder != null || it.genre != null }
}
