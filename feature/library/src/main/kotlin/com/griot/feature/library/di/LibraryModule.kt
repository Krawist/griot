package com.griot.feature.library.di

import com.griot.feature.library.LibraryItems
import com.griot.feature.library.LibraryItemsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface LibraryModule {
    @Binds
    fun bindLibraryItems(impl: LibraryItemsImpl): LibraryItems
}
