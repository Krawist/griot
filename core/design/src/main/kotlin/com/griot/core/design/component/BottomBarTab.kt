package com.griot.core.design.component

import androidx.annotation.DrawableRes

/**
 * One tab of the [GriotBottomBar].
 *
 * @property icon Drawable of the icon, tinted by the bar.
 * @property label Name of the tab, shown under the icon.
 */
data class BottomBarTab(@param:DrawableRes val icon: Int, val label: String)
