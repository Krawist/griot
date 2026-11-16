package com.griot.feature.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.griot.core.data.song.SectionLabel
import com.griot.core.data.song.words

/** The label in words, in the interface's language: the author's name, else « Couplet 2 » or « Refrain ». */
@Composable
internal fun SectionLabel.text(): String = words(LocalContext.current.resources)
