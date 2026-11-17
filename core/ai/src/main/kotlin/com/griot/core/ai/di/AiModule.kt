package com.griot.core.ai.di

import com.griot.core.ai.AiAssistant
import com.griot.core.ai.AiAssistantImpl
import com.griot.core.ai.AiProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
internal interface AiModule {
    @Binds
    fun bindAiAssistant(impl: AiAssistantImpl): AiAssistant

    // The on-device (GRI-32) and online (GRI-33) providers join this set; empty until then.
    @Multibinds
    fun aiProviders(): Set<AiProvider>
}
