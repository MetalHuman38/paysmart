package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.domain.security.SecurityPolicyEngine
import net.metalbrain.paysmart.domain.security.SecurityPolicyEngineImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SecurityPolicyBindingModule {

    @Binds
    @Singleton
    abstract fun bindSecurityPolicyEngine(
        impl: SecurityPolicyEngineImpl
    ): SecurityPolicyEngine
}
