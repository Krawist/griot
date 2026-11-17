package com.griot.feature.library

/**
 * A button of an [EmptyState].
 *
 * @property label What it says.
 * @property onClick What it does.
 */
internal data class EmptyAction(val label: String, val onClick: () -> Unit)
