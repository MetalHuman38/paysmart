package net.metalbrain.paysmart.data.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.domain.model.WalletBalanceModel

interface WalletBalanceGateway {
    fun observeByUserId(userId: String): Flow<WalletBalanceModel?>

    suspend fun syncFromServer(userId: String): Result<WalletBalanceModel?>

    suspend fun upsert(model: WalletBalanceModel)
}
