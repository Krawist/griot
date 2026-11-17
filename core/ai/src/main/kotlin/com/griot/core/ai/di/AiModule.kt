package com.griot.core.ai.di

import com.griot.core.ai.AiAssistant
import com.griot.core.ai.AiAssistantImpl
import com.griot.core.ai.AiProvider
import com.griot.core.ai.OnDeviceProvider
import com.griot.core.ai.online.AiApiFactory
import com.griot.core.ai.online.AiApiFactoryImpl
import com.griot.core.ai.online.ApiKeyRepository
import com.griot.core.ai.online.ApiKeyRepositoryImpl
import com.griot.core.ai.online.OnlineProvider
import com.griot.core.ai.online.SecretCipher
import com.griot.core.ai.online.SecretCipherImpl
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

    @Multibinds
    fun aiProviders(): Set<AiProvider>

    @Binds
    @IntoSet
    fun bindOnDeviceProvider(impl: OnDeviceProvider): AiProvider

    @Binds
    @IntoSet
    fun bindOnlineProvider(impl: OnlineProvider): AiProvider

    @Binds
    fun bindApiKeyRepository(impl: ApiKeyRepositoryImpl): ApiKeyRepository

    @Binds
    fun bindSecretCipher(impl: SecretCipherImpl): SecretCipher

    @Binds
    fun bindAiApiFactory(impl: AiApiFactoryImpl): AiApiFactory
}
