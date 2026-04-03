package net.metalbrain.paysmart.core.features.addmoney.data

enum class AddMoneyProvider {
    STRIPE,
    FLUTTERWAVE;

    companion object {
        fun fromRawOrNull(raw: String?): AddMoneyProvider? {
            return when (raw?.trim()?.lowercase()) {
                "stripe" -> STRIPE
                "flutterwave" -> FLUTTERWAVE
                else -> null
            }
        }

        fun fromRaw(raw: String?): AddMoneyProvider {
            return fromRawOrNull(raw) ?: STRIPE
        }
    }
}

enum class AddMoneySessionStatus {
    CREATED,
    PENDING,
    SUCCEEDED,
    FAILED,
    EXPIRED;

    companion object {
        fun fromRaw(raw: String?): AddMoneySessionStatus {
            return when (raw?.trim()?.lowercase()) {
                "created" -> CREATED
                "pending" -> PENDING
                "succeeded" -> SUCCEEDED
                "failed" -> FAILED
                "expired" -> EXPIRED
                else -> PENDING
            }
        }
    }
}

data class AddMoneyVirtualAccountData(
    val accountNumber: String,
    val bankName: String,
    val accountName: String? = null,
    val reference: String,
    val note: String? = null
)

data class AddMoneySessionData(
    val sessionId: String,
    val amountMinor: Int,
    val currency: String,
    val status: AddMoneySessionStatus,
    val expiresAtMs: Long,
    val provider: AddMoneyProvider = AddMoneyProvider.STRIPE,
    val checkoutUrl: String? = null,
    val flutterwaveTransactionId: String? = null,
    val virtualAccount: AddMoneyVirtualAccountData? = null,
    val paymentIntentId: String? = null,
    val paymentIntentClientSecret: String? = null,
    val publishableKey: String? = null,
    val customerId: String? = null,
    val customerEphemeralKeySecret: String? = null,
    val defaultPaymentMethodId: String? = null
)

data class AddMoneyPaymentSheetCustomer(
    val customerId: String,
    val ephemeralKeySecret: String,
    val defaultPaymentMethodId: String? = null
)

data class AddMoneyPaymentSheetLaunch(
    val publishableKey: String,
    val paymentIntentClientSecret: String,
    val customer: AddMoneyPaymentSheetCustomer? = null
)
