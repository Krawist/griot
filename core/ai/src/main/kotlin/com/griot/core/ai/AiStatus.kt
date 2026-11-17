package com.griot.core.ai

/** Whether a model can answer. */
enum class AiStatus {
    /** It answers now. */
    READY,

    /** The phone can have it, and its model is downloading: it will answer once it is done. */
    PREPARING,

    /** This phone cannot have it, or nothing is set up for it. */
    UNAVAILABLE,
}
