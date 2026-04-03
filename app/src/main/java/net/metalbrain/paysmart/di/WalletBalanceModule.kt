package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.data.repository.WalletBalanceGateway
import net.metalbrain.paysmart.data.repository.WalletBalanceRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WalletBalanceModule {

    @Binds
    @Singleton
    abstract fun bindWalletBalanceGateway(
        impl: WalletBalanceRepository
    ): WalletBalanceGateway
}
