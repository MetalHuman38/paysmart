package net.metalbrain.paysmart.core.features.cards.repository

import java.io.IOException
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardErrorCode

class ManagedCardsApiException(
    val statusCode: Int,
    val code: ManagedCardErrorCode?,
    override val message: String
) : IOException(message)
