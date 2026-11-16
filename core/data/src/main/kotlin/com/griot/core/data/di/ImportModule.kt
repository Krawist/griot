package com.griot.core.data.di

import com.griot.core.data.importing.DocumentReader
import com.griot.core.data.importing.DocumentReaderImpl
import com.griot.core.data.importing.TextImporter
import com.griot.core.data.importing.TextImporterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface ImportModule {
    @Binds
    fun bindTextImporter(impl: TextImporterImpl): TextImporter

    @Binds
    fun bindDocumentReader(impl: DocumentReaderImpl): DocumentReader
}
