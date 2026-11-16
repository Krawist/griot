package com.griot.core.data.di

import com.griot.core.data.export.MarkdownExporter
import com.griot.core.data.export.MarkdownExporterImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface ExportModule {
    @Binds
    fun bindMarkdownExporter(impl: MarkdownExporterImpl): MarkdownExporter
}
