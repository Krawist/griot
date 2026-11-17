package com.griot.core.sync.di

import com.griot.core.sync.NetworkMonitor
import com.griot.core.sync.NetworkMonitorImpl
import com.griot.core.sync.RemoteStore
import com.griot.core.sync.SyncRepository
import com.griot.core.sync.SyncRepositoryImpl
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SyncModule {
    @Binds
    fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository

    @Binds
    fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor

    // Bound by the Google Drive store (GRI-27); absent until then, and sync is off.
    @BindsOptionalOf
    fun optionalRemoteStore(): RemoteStore
}
