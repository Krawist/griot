package com.griot.feature.editor.history

private val Tokens = Regex("""\s+|[^\s]+""")

/**
 * [text] cut into pieces, the words that [other] does not have marked as changed: the words left out of their
 * longest common sequence, compared without case. Spaces between two changed words are changed too.
 */
internal fun changedWords(text: String, other: String): List<DiffSegment> {
    val tokens = Tokens.findAll(text).map { it.value }.toList()
    val words = tokens.filter { it.isNotBlank() }
    val kept =
        commonWords(
            words.map {
                it.lowercase()
            },
            Tokens.findAll(other).map { it.value.lowercase() }.filter { it.isNotBlank() }.toList(),
        )
    var word = 0
    val marked = tokens.map { token ->
        if (token.isBlank()) {
            token to null
        } else {
            token to (word++ !in kept)
        }
    }
    // A space takes the state of the words around it when both are changed.
    val segments = marked.mapIndexed { index, (token, changed) ->
        val spaceChanged = changed == null &&
            marked.getOrNull(index - 1)?.second == true &&
            marked.getOrNull(index + 1)?.second == true
        DiffSegment(text = token, changed = changed ?: spaceChanged)
    }
    return segments.fold(mutableListOf()) { merged, segment ->
        val last = merged.lastOrNull()
        if (last != null && last.changed == segment.changed) {
            merged[merged.lastIndex] = last.copy(text = last.text + segment.text)
        } else {
            merged += segment
        }
        merged
    }
}

// Indices in [words] of the words of their longest common sequence with [other].
private fun commonWords(words: List<String>, other: List<String>): Set<Int> {
    val lengths = Array(words.size + 1) { IntArray(other.size + 1) }
    for (i in words.indices.reversed()) {
        for (j in other.indices.reversed()) {
            lengths[i][j] = if (words[i] == other[j]) {
                lengths[i + 1][j + 1] + 1
            } else {
                maxOf(lengths[i + 1][j], lengths[i][j + 1])
            }
        }
    }
    val kept = mutableSetOf<Int>()
    var i = 0
    var j = 0
    while (i < words.size && j < other.size) {
        when {
            words[i] == other[j] -> {
                kept += i
                i++
                j++
            }
            lengths[i + 1][j] >= lengths[i][j + 1] -> i++
            else -> j++
        }
    }
    return kept
}
