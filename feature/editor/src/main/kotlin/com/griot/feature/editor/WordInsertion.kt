package com.griot.feature.editor

/**
 * [text] with [word] inserted at [cursor] (clamped to the text), spaces added where the word would touch another one.
 * Returns the new text and the cursor just after the word.
 */
internal fun insertWord(text: String, cursor: Int, word: String): Pair<String, Int> {
    val at = cursor.coerceIn(0, text.length)
    val before = text.substring(0, at)
    val after = text.substring(at)
    val spaceBefore = if (before.isNotEmpty() && !before.last().isWhitespace() && before.last() != '\'') " " else ""
    val spaceAfter = if (after.isNotEmpty() && after.first().isLetterOrDigit()) " " else ""
    val inserted = before + spaceBefore + word + spaceAfter + after
    return inserted to (before.length + spaceBefore.length + word.length)
}
