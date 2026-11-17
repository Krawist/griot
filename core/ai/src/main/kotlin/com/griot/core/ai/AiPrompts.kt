package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage

// Shared by every prompt: the model helps, it never writes the song.
private const val ROLE = "You help a songwriter with one verse of their song. You never write the song in their place."

/** The prompt asking three versions of the verse that keep its meaning, its rhyme and about its syllable count. */
internal fun rephrasePrompt(request: VerseRequest): AiPrompt = AiPrompt(
    instructions = """
        $ROLE
        Answer in ${request.language.englishName()}.
        Give exactly three new versions of the verse, one per line, without numbering, quotes or comments.
        Each version keeps the meaning of the verse, ends with the same rhyme sound, and has about
        ${request.targetSyllables} syllables.
    """.trimIndent(),
    request = request.context(task = "Verse to rephrase"),
)

/**
 * The prompt asking at most three short remarks on the verse, never a rewritten verse. [counts] are the syllables of
 * the verses of the section, in order, as the phonetic engine counts them: the rhythm remarks rely on them.
 */
internal fun critiquePrompt(request: VerseRequest, counts: List<Int>): AiPrompt = AiPrompt(
    instructions = """
        $ROLE
        Answer in ${request.language.englishName()}.
        Give at most three short remarks on the verse, one per line, without numbering: cliches, weak images, breaks
        of tone, rhythm compared with the other verses of the section.
        Never write a new version of the verse, nor any part of one.
    """.trimIndent(),
    request = request.context(task = "Verse to critique") +
        "\nSyllables of the verses of the section, in order: ${counts.joinToString()}",
)

/** The lines of a model's answer, without bullets, numbers nor quotes: at most [max], each once. */
internal fun answerLines(answer: String, max: Int = MAX_ITEMS): List<String> = answer.lines()
    .map { line -> line.trim().replace(Bullet, "").trim().trim('"', '«', '»', '“', '”').trim() }
    .filter { it.isNotEmpty() }
    .distinct()
    .take(max)

/** At most this many suggestions or remarks are shown. */
internal const val MAX_ITEMS = 3

// « - », « • », « * », « 1. », « 2) » at the start of a line.
private val Bullet = Regex("""^([-•*]|\d+[.)])\s*""")

private fun VerseRequest.context(task: String): String = buildString {
    appendLine("Section: $section")
    if (before.isNotEmpty()) appendLine("Verses before:\n${before.joinToString("\n")}")
    appendLine("$task: $verse")
    if (after.isNotEmpty()) appendLine("Verses after:\n${after.joinToString("\n")}")
    append("Syllables of the verse: $targetSyllables")
}

private fun PhoneticLanguage.englishName(): String = when (this) {
    PhoneticLanguage.FRENCH -> "French"
    PhoneticLanguage.ENGLISH -> "English"
}
