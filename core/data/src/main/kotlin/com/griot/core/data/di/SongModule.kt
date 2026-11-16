package com.griot.core.data.di

import com.griot.core.data.song.SongRepository
import com.griot.core.data.song.SongRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface SongModule {
    @Binds
    fun bindSongRepository(impl: SongRepositoryImpl): SongRepository
}
