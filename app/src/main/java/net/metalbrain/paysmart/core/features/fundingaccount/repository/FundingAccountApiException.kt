package net.metalbrain.paysmart.core.features.fundingaccount.repository

import net.metalbrain.paysmart.core.features.fundingaccount.data.FundingAccountErrorCode

internal class FundingAccountApiException(
    val statusCode: Int,
    val code: FundingAccountErrorCode?,
    override val message: String
) : IllegalStateException(message)
