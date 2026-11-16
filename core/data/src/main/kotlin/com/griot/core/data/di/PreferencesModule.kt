package com.griot.core.data.di

import com.griot.core.data.preferences.UserPreferencesRepository
import com.griot.core.data.preferences.UserPreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface PreferencesModule {
    @Binds
    fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
