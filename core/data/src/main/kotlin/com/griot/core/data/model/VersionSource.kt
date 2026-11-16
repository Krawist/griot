package com.griot.core.data.model

/** What created a version of a verse or of a song (GRI-24, GRI-28). */
enum class VersionSource {
    /** The author wrote it. */
    AUTHOR,

    /** The author accepted an AI suggestion: shown as « Suggestion IA ». */
    AI_SUGGESTION,

    /** The author restored an older version. */
    RESTORE,

    /** The text of another device, kept when both changed the same verse. */
    SYNC_CONFLICT,
}
