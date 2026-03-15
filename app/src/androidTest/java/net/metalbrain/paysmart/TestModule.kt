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
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.core.features.language.di.LanguageModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [
        PhoneModule::class,
        LanguageModule::class,
        AppCheckModule::class
    ]
)

object TestModule {
    @Provides
    fun provideMockAuthApiConfig(): AuthApiConfig {
        return AuthApiConfig(
            baseUrl = "https.fake.url",
            attachApiPrefix = false,
        )
    }

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
