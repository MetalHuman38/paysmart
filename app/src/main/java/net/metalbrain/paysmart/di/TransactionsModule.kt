package net.metalbrain.paysmart.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.data.repository.LocalTransactionRepository
import net.metalbrain.paysmart.data.repository.TransactionHistoryRepository
import net.metalbrain.paysmart.data.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TransactionModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: LocalTransactionRepository
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindTransactionHistoryRepository(
        impl: LocalTransactionRepository
    ): TransactionHistoryRepository
}
