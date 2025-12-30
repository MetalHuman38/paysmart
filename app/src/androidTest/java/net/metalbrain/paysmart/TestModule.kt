package net.metalbrain.paysmart

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.coEvery
import io.mockk.mockk
import net.metalbrain.paysmart.core.auth.AuthPolicyHandler
import net.metalbrain.paysmart.di.AppModule
import net.metalbrain.paysmart.domain.LanguageRepository
import net.metalbrain.paysmart.phone.PhoneVerifier

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule.PhoneModule::class]
)
object TestModule {
    @Provides
    fun provideMockAuthPolicyHandler(): AuthPolicyHandler {
        val mock = mockk<AuthPolicyHandler>()
        coEvery { mock.checkBeforeCreate(any(), any(), any()) } returns false
        return mock
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
