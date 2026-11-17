package com.griot.core.ai

import com.griot.core.phonetics.PhoneticLanguage

// Shared by every prompt: the model helps, it never writes the song.
private const val ROLE = "You help a songwriter with one verse of their song. You never write the song in their place."

/** The prompt asking three versions of the verse that keep its meaning, its rhyme and about its syllable count. */
internal fun rephrasePrompt(request: VerseRequest): AiPrompt = AiPrompt(
    instructions = """
        $ROLE
        Answer in ${request.language.englishName()}.
        Write exactly 3 new versions of the verse to rephrase, and nothing else: one version per line, without
        numbering, quotes or comments.
        Each version keeps the meaning of the verse, ends with a word that rhymes with "${request.lastWord()}", and
        has about ${request.targetSyllables} syllables.
        Never rewrite the other verses: they are only context.
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
        Never write a new version of the verse, nor any part of one, and never rewrite the other verses: they are
        only context.
    """.trimIndent(),
    request = "Syllables of the verses of the section, in order: ${counts.joinToString()}\n" +
        request.context(task = "Verse to critique"),
)

/** The prompt asking a short title for a whole song, from its first verse or its chorus. */
internal fun titlePrompt(request: SongRequest): AiPrompt = AiPrompt(
    instructions = """
        You help a songwriter name a song they wrote. You never rewrite the song.
        Answer in ${request.language.englishName()}.
        Write one title of 1 to 4 words, taken from the first verse or the chorus, and nothing else: no quotes, no
        comments.
    """.trimIndent(),
    request = "Song:\n${request.lyrics.joinToString("\n")}",
)

/** The prompt asking a folder and a genre for a song, among the names given only. */
internal fun classifyPrompt(request: SongRequest): AiPrompt = AiPrompt(
    instructions = """
        You help a songwriter file a song they wrote, from its themes and its style. You never rewrite the song.
        Answer with exactly two lines and nothing else:
        Folder: one of the folders listed, or none
        Genre: one of the genres listed, or none
    """.trimIndent(),
    request = buildString {
        appendLine("Folders: ${request.folders.joinToString().ifEmpty { "none" }}")
        appendLine("Genres: ${request.genres.joinToString()}")
        append("Song:\n${request.lyrics.joinToString("\n")}")
    },
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

// The section and the neighbouring verses as context, then the verse itself, last, where a small model looks most.
private fun VerseRequest.context(task: String): String = buildString {
    appendLine("Section: $section")
    if (before.isNotEmpty()) appendLine("Context, verses before it (do not rewrite):\n${before.joinToString("\n")}")
    if (after.isNotEmpty()) appendLine("Context, verses after it (do not rewrite):\n${after.joinToString("\n")}")
    appendLine("Syllables of the verse: $targetSyllables")
    append("$task: $verse")
}

// The last word of the verse, which the rhyme ends on.
private fun VerseRequest.lastWord(): String =
    verse.trim().split(Regex("""[^\p{L}'’-]+""")).lastOrNull { it.isNotBlank() }.orEmpty()

private fun PhoneticLanguage.englishName(): String = when (this) {
    PhoneticLanguage.FRENCH -> "French"
    PhoneticLanguage.ENGLISH -> "English"
}
