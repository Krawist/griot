package com.griot.core.design.theme

import androidx.compose.ui.unit.dp

/** Corner radii of the mockups, from the largest surface to the smallest. Pills use `CircleShape`. */
object GriotRadius {
    /** Floating bottom bar. */
    val bar = 26.dp

    /** Bottom sheets and the editor toolbar. */
    val sheet = 24.dp

    /** Cards: songs, ideas, settings groups. */
    val card = 18.dp

    /** Text fields and the search field. */
    val field = 14.dp

    /** Rows inside a card, such as a selected verse. */
    val row = 12.dp

    /** Small square buttons, such as the language button of the editor. */
    val control = 10.dp

    /** Badges, such as the language of a song. */
    val badge = 6.dp
}
