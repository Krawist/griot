package com.griot.core.data.di

import com.griot.core.data.audio.AttachmentRepository
import com.griot.core.data.audio.AttachmentRepositoryImpl
import com.griot.core.data.audio.AudioFiles
import com.griot.core.data.audio.AudioFilesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface AudioModule {
    @Binds
    fun bindAttachmentRepository(impl: AttachmentRepositoryImpl): AttachmentRepository

    @Binds
    fun bindAudioFiles(impl: AudioFilesImpl): AudioFiles
}
