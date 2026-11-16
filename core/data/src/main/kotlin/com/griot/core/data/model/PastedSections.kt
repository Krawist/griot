package com.griot.core.data.model

/**
 * What a paste added to a song, so that it can be undone.
 *
 * @property sectionIds The sections created after the section pasted into.
 * @property verseIds The verses added to the section pasted into.
 */
data class PastedSections(val sectionIds: List<String>, val verseIds: List<String>)
