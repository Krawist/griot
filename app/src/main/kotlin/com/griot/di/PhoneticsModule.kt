package com.griot.di

import com.griot.core.phonetics.PhoneticEngines
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// :core:phonetics is pure Kotlin, without Hilt: the app makes its engines, once.
@Module
@InstallIn(SingletonComponent::class)
internal object PhoneticsModule {
    @Provides
    @Singleton
    fun providePhoneticEngines(): PhoneticEngines = PhoneticEngines()
}
