package com.griot.core.data.model

import kotlinx.serialization.Serializable

/**
 * The content of a song at one moment, as a version of the whole song keeps it.
 *
 * @property sections Its sections in order, a repeat pointing to its source by index.
 */
@Serializable
data class SongSnapshot(val sections: List<NewSection>)
