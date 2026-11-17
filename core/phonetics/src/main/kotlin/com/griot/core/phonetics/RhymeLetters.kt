package com.griot.core.phonetics

private const val ALPHABET = 26

/**
 * The rhyme rank of each verse of a section, given in order of appearance (0 for A, 1 for B…), from the verses'
 * [rhymes]: verses with the same rhyme key share a rank. `null` for a verse without a rhyme (an empty verse).
 */
fun rhymeLetters(rhymes: List<Rhyme?>): List<Int?> {
    val ranks = rhymes.mapNotNull { it?.key }.distinct().withIndex().associate { (index, key) -> key to index }
    return rhymes.map { rhyme -> rhyme?.let { ranks[it.key] } }
}

/** The letter of a rhyme's rank: A for 0, B for 1…, back to A after Z. */
fun rhymeLetter(rank: Int): Char = 'A' + rank % ALPHABET

/** The rhyme scheme of [ranks], one letter per verse with a rhyme (« ABAB »). */
fun rhymeScheme(ranks: List<Int?>): String = ranks.filterNotNull().joinToString(separator = "") { "${rhymeLetter(it)}" }
