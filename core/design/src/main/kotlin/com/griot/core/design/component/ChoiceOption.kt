package com.griot.core.design.component

/**
 * One option of a [ChoiceSheet].
 *
 * @property value What choosing it gives.
 * @property title Its name.
 * @property hint What it does, under its name; none when `null`.
 */
data class ChoiceOption<T>(val value: T, val title: String, val hint: String? = null)
