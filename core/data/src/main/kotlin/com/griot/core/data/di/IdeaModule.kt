package com.griot.core.data.di

import com.griot.core.data.idea.IdeaRepository
import com.griot.core.data.idea.IdeaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface IdeaModule {
    @Binds
    fun bindIdeaRepository(impl: IdeaRepositoryImpl): IdeaRepository
}
