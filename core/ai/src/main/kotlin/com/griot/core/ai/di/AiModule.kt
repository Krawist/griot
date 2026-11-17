package com.griot.core.ai.di

import com.griot.core.ai.AiAssistant
import com.griot.core.ai.AiAssistantImpl
import com.griot.core.ai.AiProvider
import com.griot.core.ai.OnDeviceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
internal interface AiModule {
    @Binds
    fun bindAiAssistant(impl: AiAssistantImpl): AiAssistant

    // The online provider (GRI-33) joins this set.
    @Multibinds
    fun aiProviders(): Set<AiProvider>

    @Binds
    @IntoSet
    fun bindOnDeviceProvider(impl: OnDeviceProvider): AiProvider
}
