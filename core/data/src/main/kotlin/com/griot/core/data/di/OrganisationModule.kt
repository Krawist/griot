package com.griot.core.data.di

import com.griot.core.data.organisation.FolderRepository
import com.griot.core.data.organisation.FolderRepositoryImpl
import com.griot.core.data.organisation.GenreRepository
import com.griot.core.data.organisation.GenreRepositoryImpl
import com.griot.core.data.tidy.TidyUpRepository
import com.griot.core.data.tidy.TidyUpRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface OrganisationModule {
    @Binds
    fun bindFolderRepository(impl: FolderRepositoryImpl): FolderRepository

    @Binds
    fun bindGenreRepository(impl: GenreRepositoryImpl): GenreRepository

    @Binds
    fun bindTidyUpRepository(impl: TidyUpRepositoryImpl): TidyUpRepository
}
