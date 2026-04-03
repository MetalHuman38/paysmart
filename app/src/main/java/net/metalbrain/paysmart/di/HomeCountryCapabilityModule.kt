package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.capabilities.repository.HomeCountryCapabilityGatewayAdapter
import net.metalbrain.paysmart.ui.home.data.HomeCountryCapabilityGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeCountryCapabilityModule {

    @Binds
    @Singleton
    abstract fun bindHomeCountryCapabilityGateway(
        impl: HomeCountryCapabilityGatewayAdapter
    ): HomeCountryCapabilityGateway
}
