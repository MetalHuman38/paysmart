package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.sendmoney.data.HomeRecentRecipientGatewayAdapter
import net.metalbrain.paysmart.ui.home.data.HomeRecentRecipientGateway
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRecentRecipientModule {

    @Binds
    @Singleton
    abstract fun bindHomeRecentRecipientGateway(
        impl: HomeRecentRecipientGatewayAdapter
    ): HomeRecentRecipientGateway
}
