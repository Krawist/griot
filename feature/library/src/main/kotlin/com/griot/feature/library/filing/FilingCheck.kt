package com.griot.feature.library.filing

/** Whether the songs being filed are in a folder, or have a genre: all of them, none, or some only. */
internal enum class FilingCheck {
    CHECKED,
    UNCHECKED,

    /** Some of the songs selected only: it stays so unless the author taps it. */
    MIXED,
}
