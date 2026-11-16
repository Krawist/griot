package com.griot.core.phonetics

import java.util.zip.GZIPInputStream

/**
 * A word → phonemes table read from a gzipped resource of `word<TAB>phonemes` lines sorted by word (built by
 * `tools/phonetics/build_tables.py`). Kept as two sorted arrays: about half the memory of a map, found by binary
 * search. Read once, on first use.
 */
internal class PhoneticDictionary(private val resource: String) {
    private val table: Pair<Array<String>, Array<String>> by lazy { read() }

    /** The phonemes of [word], in lower case; `null` when the dictionary does not have it. */
    operator fun get(word: String): String? {
        val (words, phonemes) = table
        val index = words.binarySearch(word)
        return if (index >= 0) phonemes[index] else null
    }

    private fun read(): Pair<Array<String>, Array<String>> {
        val stream = requireNotNull(javaClass.getResourceAsStream(resource)) { "Missing resource $resource" }
        val words = ArrayList<String>(TABLE_SIZE)
        val phonemes = ArrayList<String>(TABLE_SIZE)
        GZIPInputStream(stream).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                val tab = line.indexOf('\t')
                if (tab > 0) {
                    words += line.substring(0, tab)
                    phonemes += line.substring(tab + 1)
                }
            }
        }
        return words.toTypedArray() to phonemes.toTypedArray()
    }

    private companion object {
        // Both dictionaries hold about 126 000 words.
        const val TABLE_SIZE = 130_000
    }
}
