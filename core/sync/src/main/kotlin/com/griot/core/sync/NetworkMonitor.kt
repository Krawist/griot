package com.griot.core.sync

import kotlinx.coroutines.flow.Flow

/** Whether the phone can reach the internet. */
interface NetworkMonitor {
    /** `true` while a network with internet is available, updated on each change. */
    val online: Flow<Boolean>
}
