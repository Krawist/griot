package com.griot.core.data.model

/** What an audio of a song is. */
enum class AttachmentKind {
    /** A voice memo recorded in Griot (GRI-50), kept in the app's files. */
    MEMO,

    /** A file of the phone, never copied: Griot keeps its address and the right to read it. */
    FILE,

    /** A link to a page: Spotify, YouTube, SoundCloud or any address. */
    LINK,
}
