package com.griot.feature.library.filing

/** Whether the songs being filed are in a folder: all of them, none, or some only. */
internal enum class FolderCheck {
    CHECKED,
    UNCHECKED,

    /** Some of the songs selected are in it: it stays so unless the author taps it. */
    MIXED,
}
