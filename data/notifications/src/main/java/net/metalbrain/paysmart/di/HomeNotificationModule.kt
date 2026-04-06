package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.notifications.HomeNotificationGatewayAdapter
import net.metalbrain.paysmart.ui.home.data.HomeNotificationGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeNotificationModule {

    @Binds
    @Singleton
    abstract fun bindHomeNotificationGateway(
        impl: HomeNotificationGatewayAdapter
    ): HomeNotificationGateway
}
