package com.griot.core.data.model

/**
 * A genre a song can be tagged with.
 *
 * @property id Stable id, the same on every device; the built-in genres have fixed ids.
 * @property name The name the author gave; empty for a built-in genre.
 * @property builtIn Which built-in genre it is, named by the interface's language; `null` for a genre of the author,
 * which can be renamed or deleted.
 */
data class Genre(val id: String, val name: String, val builtIn: BuiltInGenre?)
