package net.metalbrain.paysmart.ui.account.recovery.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.ui.account.recovery.auth.ChangePhoneRecoveryAuthGateway
import net.metalbrain.paysmart.ui.account.recovery.auth.gateway.FirebaseChangePhoneRecoveryAuthGateway

@Module
@InstallIn(SingletonComponent::class)
abstract class RecoveryModule {
    @Binds
    abstract fun bindChangePhoneRecoveryAuthGateway(
        impl: FirebaseChangePhoneRecoveryAuthGateway
    ): ChangePhoneRecoveryAuthGateway
}
