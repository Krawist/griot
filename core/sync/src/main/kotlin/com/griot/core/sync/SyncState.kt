package com.griot.core.sync

/** Where sync stands, for the library, the editor and the settings. */
enum class SyncState {
    /** No remote store is set up, or sync is turned off: everything stays on the phone. */
    OFF,

    /** A sync is running. */
    SYNCING,

    /** Every change is on the remote store. */
    SYNCED,

    /** Changes wait to be pushed. */
    PENDING,

    /** No network: changes are kept and wait for it. */
    OFFLINE,

    /** The last sync failed; changes are kept and the next sync tries again. */
    ERROR,
}
