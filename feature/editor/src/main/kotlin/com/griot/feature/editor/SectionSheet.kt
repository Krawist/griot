package com.griot.feature.editor

/** A sheet opened from the menu of a section. */
internal enum class SectionSheet {
    /** The kinds of section to add right below it. */
    ADD_BELOW,

    /** The field to rename it. */
    RENAME,

    /** The confirmation of its delete. */
    DELETE,
}
