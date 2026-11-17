package com.griot.core.sync

/** How a sync ended, for the job that runs it to retry or not. */
enum class SyncResult {
    /** Pulled and pushed everything. */
    DONE,

    /** No remote store is set up: nothing to do. */
    OFF,

    /** No network: to try again once it is back. */
    OFFLINE,

    /** The remote store failed: to try again later. */
    FAILED,
}
