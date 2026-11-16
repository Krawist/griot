package com.griot.core.phonetics.paste

import java.util.Locale

// « [Refrain] », « (Chorus x2) », « Couplet 2 : », « Pré-refrain » alone on their line.
private val Label = Regex(
    """^\s*[\[(]?\s*(couplet|refrain|pont|intro|outro|verse|chorus|bridge|pr[eé][- ]?(?:refrain|chorus))""" +
        """\s*\d*\s*(?:[x×]\s*\d+)?\s*[\])]?\s*:?\s*$""",
    RegexOption.IGNORE_CASE,
)

private val Kinds = mapOf(
    "couplet" to BlockKind.VERSE,
    "verse" to BlockKind.VERSE,
    "refrain" to BlockKind.CHORUS,
    "chorus" to BlockKind.CHORUS,
    "pont" to BlockKind.BRIDGE,
    "bridge" to BlockKind.BRIDGE,
    "intro" to BlockKind.INTRO,
    "outro" to BlockKind.OUTRO,
)

/**
 * Splits a pasted text into sections: a blank line starts a new section, a label line (« [Refrain] », « Couplet 2 : »)
 * names the section that follows and is not kept as a verse, a block identical to an earlier one repeats it, and a
 * label alone repeats the last section of its kind. A text with no blank line and no label stays one block.
 */
fun splitPastedText(text: String): List<PastedBlock> {
    val raw = mutableListOf<PastedBlock>()
    var kind: BlockKind? = null
    var label: String? = null
    var lines = mutableListOf<String>()
    var labelled = false
    fun close() {
        if (lines.isNotEmpty() ||
            labelled
        ) {
            raw += PastedBlock(kind = kind, label = label, lines = lines, repeatOf = null)
        }
        kind = null
        label = null
        lines = mutableListOf()
        labelled = false
    }
    text.lines().map { it.trimEnd() }.forEach { line ->
        val match = Label.matchEntire(line)
        when {
            // A label then a blank line still names the block below it.
            line.isBlank() -> if (lines.isNotEmpty()) close()
            match != null -> {
                close()
                val word = match.groupValues[1]
                kind = Kinds[word.lowercase(Locale.ROOT)] ?: BlockKind.PRE_CHORUS
                label = word
                labelled = true
            }
            else -> lines += line.trim()
        }
    }
    close()
    return withRepeats(raw)
}

// Lone labels repeat the last block of their kind (dropped when there is none); identical blocks repeat the first.
private fun withRepeats(raw: List<PastedBlock>): List<PastedBlock> {
    val blocks = mutableListOf<PastedBlock>()
    raw.forEach { block ->
        if (block.lines.isEmpty()) {
            val source = blocks.indexOfLast { it.kind == block.kind && it.repeatOf == null && it.lines.isNotEmpty() }
            if (source >= 0) blocks += block.copy(repeatOf = source)
        } else {
            val source = blocks.indexOfFirst { it.repeatOf == null && it.lines.same(block.lines) }
            if (source >= 0) {
                // A plain block sung again is a chorus.
                val original = blocks[source]
                val sourceKind = original.kind ?: BlockKind.CHORUS
                blocks[source] = original.copy(kind = sourceKind)
                blocks += block.copy(kind = block.kind ?: sourceKind, repeatOf = source)
            } else {
                blocks += block
            }
        }
    }
    return blocks
}

private fun List<String>.same(other: List<String>): Boolean =
    size == other.size && zip(other).all { (a, b) -> a.equals(b, ignoreCase = true) }
