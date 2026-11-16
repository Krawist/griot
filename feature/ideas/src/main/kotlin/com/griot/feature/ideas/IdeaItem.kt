package com.griot.feature.ideas

import com.griot.core.data.model.Idea
import com.griot.core.design.format.RelativeTime

/**
 * One card of the ideas box.
 *
 * @property idea The idea.
 * @property saved When it was saved, as the card reads it.
 */
internal data class IdeaItem(val idea: Idea, val saved: RelativeTime)
