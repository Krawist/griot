package com.griot.core.phonetics

/** Rhyming words shown per number of syllables. */
internal const val RHYMES_PER_COUNT = 12

/** Words of more syllables than this are left out of the rhyme suggestions. */
internal const val MAX_RHYME_SYLLABLES = 4

/**
 * The rhymes to suggest among [candidates]: by number of syllables, from 1 to [MAX_RHYME_SYLLABLES], each count
 * holding its [RHYMES_PER_COUNT] best words by [rank] (lower first), then the shortest, then in alphabetical order.
 */
internal fun chooseRhymes(candidates: List<RhymingWord>, rank: (String) -> Int): List<RhymingWord> = candidates
    .filter { it.syllables in 1..MAX_RHYME_SYLLABLES }
    .distinctBy { it.word }
    .groupBy { it.syllables }
    .toSortedMap()
    .values
    .flatMap { words ->
        words.sortedWith(compareBy<RhymingWord>({ rank(it.word) }, { it.word.length }, { it.word }))
            .take(RHYMES_PER_COUNT)
    }
