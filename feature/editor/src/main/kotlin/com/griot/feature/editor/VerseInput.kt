package com.griot.feature.editor

/**
 * What typing in a verse does, each given the verse's id.
 *
 * @property onChange The verse's text changed.
 * @property onLines A line break was typed or pasted: the lines, and where the cursor is in the last one.
 * @property onBackspaceAtStart Backspace with the cursor at the start: the verse's text.
 * @property onFocusHandled The verse asked to take the focus has it.
 */
internal data class VerseInput(
    val onChange: (String, String) -> Unit,
    val onLines: (String, List<String>, Int) -> Unit,
    val onBackspaceAtStart: (String, String) -> Unit,
    val onFocusHandled: () -> Unit,
)
