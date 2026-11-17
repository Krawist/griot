package com.griot.core.sync

import kotlinx.coroutines.flow.MutableStateFlow

/** [NetworkMonitor] for tests, online unless told otherwise. */
internal class FakeNetworkMonitor(online: Boolean = true) : NetworkMonitor {
    override val online = MutableStateFlow(online)
}
