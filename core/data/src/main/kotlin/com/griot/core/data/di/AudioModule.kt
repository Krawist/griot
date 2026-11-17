package com.griot.core.data.di

import com.griot.core.data.audio.AttachmentRepository
import com.griot.core.data.audio.AttachmentRepositoryImpl
import com.griot.core.data.audio.AudioFiles
import com.griot.core.data.audio.AudioFilesImpl
import com.griot.core.data.audio.AudioPlayer
import com.griot.core.data.audio.AudioPlayerImpl
import com.griot.core.data.audio.CoverReader
import com.griot.core.data.audio.CoverReaderImpl
import com.griot.core.data.audio.VoiceRecorder
import com.griot.core.data.audio.VoiceRecorderImpl
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

    @Binds
    fun bindVoiceRecorder(impl: VoiceRecorderImpl): VoiceRecorder

    @Binds
    fun bindAudioPlayer(impl: AudioPlayerImpl): AudioPlayer

    @Binds
    fun bindCoverReader(impl: CoverReaderImpl): CoverReader
}
