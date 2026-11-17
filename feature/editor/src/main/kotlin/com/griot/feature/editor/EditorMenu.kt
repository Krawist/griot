package com.griot.feature.editor

/** A menu of the editor, open over the song: one at a time. */
internal sealed interface EditorMenu {
    /** The song's « ⋯ » menu, in the top bar. */
    data object Song : EditorMenu

    /**
     * The « ⋯ » menu of a section.
     *
     * @property sectionId The id of the section.
     */
    data class Section(val sectionId: String) : EditorMenu
}
