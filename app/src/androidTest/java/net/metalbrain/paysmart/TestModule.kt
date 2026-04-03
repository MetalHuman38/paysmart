package net.metalbrain.paysmart

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.auth.appcheck.di.AppCheckModule
import net.metalbrain.paysmart.core.auth.appcheck.provider.AppCheckTokenProvider
import net.metalbrain.paysmart.core.features.account.creation.phone.data.PhoneVerifier
import net.metalbrain.paysmart.core.features.account.creation.phone.di.PhoneModule
import net.metalbrain.paysmart.core.runtime.di.ApiPrefixedAuthConfig
import net.metalbrain.paysmart.core.runtime.di.ApiRootAuthConfig
import net.metalbrain.paysmart.core.runtime.di.RuntimeConfigModule
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.core.features.language.di.LanguageModule
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        PhoneModule::class,
        LanguageModule::class,
        AppCheckModule::class,
        RuntimeConfigModule::class
    ]
)

object TestModule {
    private val fakeConfig = AuthApiConfig(baseUrl = "https://fake.url", attachApiPrefix = false)

    @Provides
    @Singleton
    @ApiRootAuthConfig
    fun provideMockRootAuthApiConfig(): AuthApiConfig = fakeConfig

    @Provides
    @Singleton
    @ApiPrefixedAuthConfig
    fun provideMockPrefixedAuthApiConfig(): AuthApiConfig =
        AuthApiConfig(baseUrl = "https://fake.url", attachApiPrefix = true)

    @Provides
    fun provideMockAuthApiConfig(): AuthApiConfig = fakeConfig

    @Provides
    fun provideMockAppCheckTokenProvider(): AppCheckTokenProvider {
        return mockk(relaxed = true)
    }

    @Provides
    fun provideMockPhoneVerifier(): PhoneVerifier {
        return mockk(relaxed = true)
    }

    @Provides
    fun providerMockLanguageRepository(): LanguageRepository {
        return mockk(relaxed = true)
    }
}
