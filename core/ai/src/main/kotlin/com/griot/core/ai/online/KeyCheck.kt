package com.griot.core.ai.online

/** What Anthropic says of a key of the online model. */
enum class KeyCheck {
    /** The key works: it is saved. */
    ACCEPTED,

    /** Anthropic refuses the key: wrong, revoked, or not an API key. */
    REFUSED,

    /** Anthropic could not be reached: nothing is saved, the author tries again. */
    UNREACHABLE,
}
