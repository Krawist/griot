package com.griot.core.data.model

/** A kind of tidy-up proposal (GRI-59). */
enum class TidyUpKind {
    /** A folder or a genre for a song, from its themes and style: from the AI. */
    CLASSIFY,

    /** A title for a song « Sans titre », from its first verse or its chorus: from the AI. */
    TITLE,

    /** Songs imported twice, found by code on their normalised text. */
    DUPLICATE,

    /** Drafts untouched for more than 30 days, moved to « Archives »: found by code. */
    ARCHIVE,
}
