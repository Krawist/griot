package com.griot.core.data.di

import com.griot.core.data.language.AppLanguageRepository
import com.griot.core.data.language.AppLanguageRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface LanguageModule {
    @Binds
    fun bindAppLanguageRepository(impl: AppLanguageRepositoryImpl): AppLanguageRepository
}
