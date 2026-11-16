package com.griot.core.design.component

/**
 * One button of a [PlaceholderScreen].
 *
 * @property label text of the button.
 * @property onClick called when the button is tapped.
 */
data class PlaceholderAction(val label: String, val onClick: () -> Unit)
