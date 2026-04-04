package net.metalbrain.paysmart.core.features.fundingaccount.repository

import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountData
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountKyc
import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountProvisionResult

interface FundingAccountGateway {
    fun observeCurrent(): Flow<FundingAccountData?>

    suspend fun syncFromServer(): Result<FundingAccountData?>

    suspend fun provision(kyc: FundingAccountKyc? = null): Result<FundingAccountProvisionResult>
}
