package net.metalbrain.paysmart.core.features.addmoney.repository

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyErrorCode

internal class AddMoneyApiException(
    val statusCode: Int,
    val code: AddMoneyErrorCode?,
    override val message: String
) : IllegalStateException(message)
