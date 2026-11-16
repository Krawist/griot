package com.griot.core.data.export

import com.griot.core.data.model.SongSection

// Characters a file name cannot hold on Android, Windows or macOS.
private val ForbiddenInFileName = Regex("""[\\/:*?"<>|\n\r\t]""")

private val Spaces = Regex("""\s+""")

/**
 * A song in Markdown: its [title] as the heading, the [author] in italics, then each section's label as a heading
 * and its verses, one per line. A repeat is written out in full, so the file reads alone in any text editor.
 *
 * @param labels The label of each section, in the order of [sections].
 */
fun songMarkdown(title: String, author: String?, sections: List<SongSection>, labels: List<String>): String =
    buildString {
        append("# ").append(title).append("\n\n")
        if (!author.isNullOrBlank()) append("*").append(author.trim()).append("*\n\n")
        sections.forEachIndexed { index, section ->
            append("## ").append(labels[index]).append("\n\n")
            // Two spaces end a line in Markdown, so each verse stays on its own line once rendered.
            append(section.verses.joinToString(separator = "  \n") { it.text.trim() })
            append("\n\n")
        }
    }.trimEnd() + "\n"

/** A file name for [title] and [extension], without the characters a file system refuses; [fallback] when blank. */
fun fileName(title: String, fallback: String, extension: String): String =
    "${title.replace(ForbiddenInFileName, " ").replace(Spaces, " ").trim().ifBlank { fallback }}.$extension"

/** [names] made unique in order: a name met again gets « (2) », « (3) »… before its extension. */
fun uniqueNames(names: List<String>): List<String> {
    val seen = mutableMapOf<String, Int>()
    return names.map { name ->
        val count = seen.merge(name.lowercase(), 1, Int::plus) ?: 1
        if (count == 1) {
            name
        } else {
            "${name.substringBeforeLast('.')} ($count).${name.substringAfterLast('.')}"
        }
    }
}
