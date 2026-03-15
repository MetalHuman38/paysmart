package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.core.features.fundingaccount.repository.FundingAccountGateway
import net.metalbrain.paysmart.core.features.fundingaccount.repository.FundingAccountRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FundingAccountModule {

    @Binds
    @Singleton
    abstract fun bindFundingAccountGateway(
        impl: FundingAccountRepository
    ): FundingAccountGateway
}
