package com.griot.core.phonetics

import java.util.zip.GZIPInputStream

/**
 * The rank of the most frequent words, from a gzipped resource of one word per line, most frequent first (built by
 * `tools/phonetics/build_tables.py`). Read once, on first use.
 */
internal class WordRanks(private val resource: String) {
    private val ranks: Map<String, Int> by lazy { read() }

    /** The rank of [word], 0 for the most frequent; [Int.MAX_VALUE] for a word not in the list. */
    operator fun get(word: String): Int = ranks[word] ?: Int.MAX_VALUE

    private fun read(): Map<String, Int> {
        val stream = requireNotNull(javaClass.getResourceAsStream(resource)) { "Missing resource $resource" }
        val ranks = HashMap<String, Int>()
        GZIPInputStream(stream).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEachIndexed { index, word -> ranks.putIfAbsent(word, index) }
        }
        return ranks
    }
}
