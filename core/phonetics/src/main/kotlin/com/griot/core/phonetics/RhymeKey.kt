package com.griot.core.phonetics

// Consonants heard close at the end of a rhyme, each written as the first of its family: the nasals (m, n, gn, ng),
// then each consonant and its voiced pair (p b, t d, k g, f v, s z, ch j, th dh).
private val CloseConsonants = mapOf(
    'm' to 'n',
    'N' to 'n',
    'G' to 'n',
    'b' to 'p',
    'd' to 't',
    'g' to 'k',
    'v' to 'f',
    'z' to 's',
    'Z' to 'S',
    'ð' to 'θ',
)

/**
 * The rhyme key of a sounded [ending] in Lexique's alphabet: its close consonants written alike (« vite » and « vide »,
 * « crime » and « cline »), its vowels as they are.
 */
internal fun rhymeKey(ending: String): String = ending.map { CloseConsonants[it] ?: it }.joinToString(separator = "")
