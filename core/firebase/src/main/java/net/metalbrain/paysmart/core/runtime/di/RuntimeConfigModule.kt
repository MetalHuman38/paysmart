package net.metalbrain.paysmart.core.runtime.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.auth.AuthApiConfig
import net.metalbrain.paysmart.core.common.runtime.AppVersionInfo
import net.metalbrain.paysmart.core.common.runtime.RuntimeConfig
import net.metalbrain.paysmart.core.runtime.BuildAppVersionInfo
import net.metalbrain.paysmart.core.runtime.BuildRuntimeConfig
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiRootAuthConfig

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiPrefixedAuthConfig

@Module
@InstallIn(SingletonComponent::class)
abstract class RuntimeBindingModule {
    @Binds
    @Singleton
    abstract fun bindRuntimeConfig(
        impl: BuildRuntimeConfig
    ): RuntimeConfig

    @Binds
    @Singleton
    abstract fun bindAppVersionInfo(
        impl: BuildAppVersionInfo
    ): AppVersionInfo
}

@Module
@InstallIn(SingletonComponent::class)
object RuntimeConfigModule {
    @Provides
    @Singleton
    @ApiRootAuthConfig
    fun provideRootAuthApiConfig(
        runtimeConfig: RuntimeConfig
    ): AuthApiConfig {
        return AuthApiConfig(
            baseUrl = runtimeConfig.authBaseUrl,
            attachApiPrefix = false
        )
    }

    @Provides
    @Singleton
    @ApiPrefixedAuthConfig
    fun provideApiPrefixedAuthApiConfig(
        runtimeConfig: RuntimeConfig
    ): AuthApiConfig {
        return AuthApiConfig(
            baseUrl = runtimeConfig.apiBaseUrl,
            attachApiPrefix = true
        )
    }

    @Provides
    @Singleton
    fun provideDefaultAuthApiConfig(
        @ApiRootAuthConfig config: AuthApiConfig
    ): AuthApiConfig {
        return config
    }
}
