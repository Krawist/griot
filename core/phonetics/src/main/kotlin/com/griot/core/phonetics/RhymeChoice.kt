package com.griot.core.phonetics

/** Rhyming words suggested for a verse, all counts together. */
internal const val RHYMES_SHOWN = 10

/** Words of more syllables than this are left out of the rhyme suggestions. */
internal const val MAX_RHYME_SYLLABLES = 4

/**
 * The [RHYMES_SHOWN] strongest rhymes among [candidates], of 1 to [MAX_RHYME_SYLLABLES] syllables, one word per
 * pronunciation by [sound] (the best ranked: « accord » rather than « accords »): the common words
 * (ranked by [rank], `Int.MAX_VALUE` for the others) first, the rich rhymes by [strength] first among them, then the
 * best ranked, the shortest, in alphabetical order. They are given by number of syllables, in that order within each.
 */
internal fun chooseRhymes(
    candidates: List<RhymingWord>,
    sound: (String) -> String,
    strength: (String) -> Int,
    rank: (String) -> Int,
): List<RhymingWord> = candidates
    .filter { it.syllables in 1..MAX_RHYME_SYLLABLES }
    .distinctBy { it.word }
    .sortedWith(compareBy<RhymingWord>({ rank(it.word) }, { it.word.length }, { it.word }))
    .distinctBy { sound(it.word) }
    .sortedWith(
        compareBy<RhymingWord>(
            { rank(it.word) == Int.MAX_VALUE },
            { -strength(it.word) },
            { rank(it.word) },
            { it.word.length },
            { it.word },
        ),
    )
    .take(RHYMES_SHOWN)
    .sortedBy { it.syllables }

/**
 * How rich the rhyme of the [candidate] sounds is with the verse's word [sounds], whose rhyming ending has
 * [endingLength] sounds: 1 when they share a sound before it (« encore » and « décor »), else 0. A word holding the
 * verse's whole word (« night » in « tonight ») is no richer than another rhyme.
 */
internal fun <T> richness(sounds: List<T>, candidate: List<T>, endingLength: Int): Int {
    val shared = sounds.asReversed().zip(candidate.asReversed()).takeWhile { (a, b) -> a == b }.size
    return if (shared in endingLength + 1 until sounds.size) 1 else 0
}
