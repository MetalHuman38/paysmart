package net.metalbrain.paysmart

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.di.AppModule
import net.metalbrain.paysmart.room.di.RoomProvidesModule
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.phone.PhoneVerifier

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RoomProvidesModule::class, AppModule.PhoneModule::class]
)
object TestModule {
    @Provides
    fun provideMockAuthApiConfig(): AuthApiConfig {
        return AuthApiConfig(
            baseUrl = "https://fake.url",
            attachApiPrefix = false,
        )
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
