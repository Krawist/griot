package com.griot.core.ai.online

/** What the provider says of a key of the online model. */
enum class KeyCheck {
    /** The key works: it is saved. */
    ACCEPTED,

    /** The provider refuses the key: wrong, revoked, or of no provider Griot knows. */
    REFUSED,

    /** The provider could not be reached: nothing is saved, the author tries again. */
    UNREACHABLE,
}
