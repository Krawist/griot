package com.griot.core.ai

// A passage in quotes, of any kind: « … », "…", “…”.
private val Quoted = Regex("""«\s*([^»]+?)\s*»|"([^"]+)"|“([^”]+)”""")

// From this many words, a quoted passage that no verse has is a rewritten verse.
private const val REWRITE_WORDS = 3

/**
 * The [remarks] that hold no rewritten verse: a remark quoting [REWRITE_WORDS] words or more that are in no verse of
 * the section of [request] is dropped. Quoting the author's own words stays allowed.
 */
internal fun keepRemarks(remarks: List<String>, request: VerseRequest): List<String> {
    val section = (request.before + request.verse + request.after).joinToString(" ") { normalized(it) }
    return remarks.filter { remark ->
        Quoted.findAll(remark).none { match ->
            val quote = match.groupValues.drop(1).first { it.isNotEmpty() }
            normalized(quote).split(' ').size >= REWRITE_WORDS && normalized(quote) !in section
        }
    }
}

// Lower case, punctuation left out, single spaces: « Le soleil, cogne ! » and « le soleil cogne » compare equal.
private fun normalized(text: String): String = text.lowercase().replace(Regex("""[^\p{L}\p{N}'’]+"""), " ").trim()
