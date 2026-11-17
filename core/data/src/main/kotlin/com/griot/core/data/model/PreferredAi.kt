package com.griot.core.data.model

/** The model the AI help asks first; the other one answers when it cannot. */
enum class PreferredAi {
    /** The model on the phone: the text does not leave it. */
    DEVICE,

    /** The online model, once its key is saved. */
    ONLINE,
}
