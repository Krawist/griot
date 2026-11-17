package com.griot.core.ai

/**
 * Where the AI help would answer from.
 *
 * @property location Where the model that would answer runs; `null` when none can answer now.
 * @property preparing Whether a model is downloading, so that the help comes once it is done.
 */
data class AiAvailability(val location: AiLocation?, val preparing: Boolean)
