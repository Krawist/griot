package com.griot.feature.onboarding.name

/**
 * What the name screen shows.
 *
 * @property name The name as typed.
 * @property emptyNameShown Whether to point out that the name is empty: after a first « Continuer » with no name.
 */
internal data class NameUiState(val name: String = "", val emptyNameShown: Boolean = false)
