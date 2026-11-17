package com.griot.core.data.di

import com.griot.core.data.sync.LocalSyncSource
import com.griot.core.data.sync.LocalSyncSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SyncModule {
    @Binds
    fun bindLocalSyncSource(impl: LocalSyncSourceImpl): LocalSyncSource
}
