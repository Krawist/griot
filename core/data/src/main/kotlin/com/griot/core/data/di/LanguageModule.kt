package com.griot.core.data.di

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.language.AppLanguageRepositoryImpl
import com.griot.core.data.language.SongLanguageDetector
import com.griot.core.data.language.SongLanguageDetectorImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface LanguageModule {
    @Binds
    fun bindAppLanguageRepository(impl: AppLanguageRepositoryImpl): AppLanguageRepository

    @Binds
    fun bindSongLanguageDetector(impl: SongLanguageDetectorImpl): SongLanguageDetector

    companion object {
        /** ML Kit's language identification; its model ships with the app, so it works offline. */
        @Provides
        @Singleton
        fun provideLanguageIdentifier(): LanguageIdentifier = LanguageIdentification.getClient()
    }
}
