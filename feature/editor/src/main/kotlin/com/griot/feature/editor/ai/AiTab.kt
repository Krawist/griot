package com.griot.feature.editor.ai

/** A tab of the AI panel. */
internal enum class AiTab {
    /** Words that rhyme with the verse, from the dictionary: on the phone, without any model. */
    RHYMES,

    /** Versions of the verse proposed by a model. */
    REPHRASE,

    /** Remarks on the verse from a model. */
    CRITIQUE,
}
