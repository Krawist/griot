package com.griot.core.phonetics

// Letters, with the apostrophes and hyphens inside a word (« aujourd'hui », « peut-être »).
private val WordPattern = Regex("""\p{L}+(?:['’-]\p{L}+)*""")

/** The words of [verse], in order. */
internal fun wordsOf(verse: String): List<Word> = WordPattern.findAll(verse).map { match ->
    Word(text = match.value.replace('’', '\''), start = match.range.first, end = match.range.last + 1)
}.toList()

private const val VOWEL_LETTERS = "aeiouyàâäéèêëîïôöùûüÿœæ"

/** Whether this letter is a vowel, accented or not. */
internal fun Char.isVowelLetter(): Boolean = lowercaseChar() in VOWEL_LETTERS

/**
 * The groups of vowel letters of [word], each as the range of its letters. The u of « qu », and of « gu » before e, i
 * or y, is part of the consonant (« tranquille », « guerre »).
 */
internal fun vowelGroups(word: String): List<IntRange> {
    val groups = mutableListOf<IntRange>()
    var start = -1
    word.forEachIndexed { index, letter ->
        if (letter.isVowelLetter() && !word.isConsonantU(index)) {
            if (start < 0) start = index
        } else if (start >= 0) {
            groups += start until index
            start = -1
        }
    }
    if (start >= 0) groups += start until word.length
    return groups
}

/**
 * Where the rhyming ending of [word] starts: at the last group of vowel letters once the [silent] ending is left out
 * (« ville » → « ille », « nuit » → « uit »); 0 for a word without vowel.
 */
internal fun rhymeStart(word: String, silent: Int): Int {
    val sounded = word.substring(0, (word.length - silent).coerceAtLeast(1))
    return vowelGroups(sounded).lastOrNull()?.first ?: 0
}

// A u heard as part of the consonant before it: « qu », or « gu » before e, i or y.
private fun String.isConsonantU(index: Int): Boolean {
    if (this[index].lowercaseChar() != 'u' || index == 0) return false
    val before = this[index - 1].lowercaseChar()
    val after = getOrNull(index + 1)?.lowercaseChar()
    return before == 'q' || (before == 'g' && after != null && after in "eiyéèê")
}
