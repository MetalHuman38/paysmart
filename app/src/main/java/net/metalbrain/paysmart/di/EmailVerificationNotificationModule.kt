package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.account.authentication.email.data.EmailVerificationNotificationGateway
import net.metalbrain.paysmart.core.notifications.EmailVerificationNotificationGatewayAdapter
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EmailVerificationNotificationModule {

    @Binds
    @Singleton
    abstract fun bindEmailVerificationNotificationGateway(
        adapter: EmailVerificationNotificationGatewayAdapter
    ): EmailVerificationNotificationGateway
}
