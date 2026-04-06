package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.metalbrain.paysmart.core.features.notifications.data.NotificationUpdateActionGateway
import net.metalbrain.paysmart.core.service.update.NotificationUpdateActionGatewayAdapter

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationUpdateActionModule {

    @Binds
    @Singleton
    abstract fun bindNotificationUpdateActionGateway(
        impl: NotificationUpdateActionGatewayAdapter
    ): NotificationUpdateActionGateway
}
