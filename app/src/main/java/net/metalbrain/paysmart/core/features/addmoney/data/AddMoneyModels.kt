package net.metalbrain.paysmart.core.features.addmoney.data

import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.core.features.fx.data.FxQuote
import net.metalbrain.paysmart.core.features.fx.data.FxQuoteDataSource

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

data class AddMoneySessionData(
    val sessionId: String,
    val amountMinor: Int,
    val currency: String,
    val status: AddMoneySessionStatus,
    val expiresAtMs: Long,
    val provider: AddMoneyProvider = AddMoneyProvider.STRIPE,
    val checkoutUrl: String? = null,
    val flutterwaveTransactionId: String? = null,
    val paymentIntentId: String? = null,
    val paymentIntentClientSecret: String? = null,
    val publishableKey: String? = null
)

data class AddMoneyPaymentSheetLaunch(
    val publishableKey: String,
    val paymentIntentClientSecret: String
)

data class AddMoneyUiState(
    val amountInput: String = "20.00",
    val currency: String = "GBP",
    val countryIso2: String = "GB",
    val countryName: String = "United Kingdom",
    val countryFlagEmoji: String = "\uD83C\uDDEC\uD83C\uDDE7",
    val countryCurrencyCode: String = "GBP",
    val topUpPolicyHint: String = "",
    val availableMethods: List<FxPaymentMethod> = FxPaymentMethod.entries,
    val countryCapabilities: List<CapabilityItem> = emptyList(),
    val availableAddMoneyProviders: List<AddMoneyProvider> = listOf(AddMoneyProvider.STRIPE),
    val quoteTargetCurrency: String = "EUR",
    val quoteMethod: FxPaymentMethod = FxPaymentMethod.WIRE,
    val isQuoteLoading: Boolean = false,
    val quote: FxQuote? = null,
    val quoteDataSource: FxQuoteDataSource? = null,
    val quoteError: String? = null,
    val isSubmitting: Boolean = false,
    val isCheckingStatus: Boolean = false,
    val sessionId: String? = null,
    val activeSessionProvider: AddMoneyProvider? = null,
    val activeSessionMethod: FxPaymentMethod? = null,
    val sessionStatus: AddMoneySessionStatus? = null,
    val providerActionUrl: String? = null,
    val paymentSheetLaunch: AddMoneyPaymentSheetLaunch? = null,
    val error: AddMoneyUiError? = null,
    val infoMessage: String? = null
) {
    val hasSession: Boolean
        get() = !sessionId.isNullOrBlank()

    val hasAvailableProvider: Boolean
        get() = availableAddMoneyProviders.isNotEmpty()

    val canSubmit: Boolean
        get() = hasAvailableProvider &&
            !isSubmitting &&
            amountInput.toDoubleOrNull()?.let { it > 0.0 } == true

    val canRefreshQuote: Boolean
        get() = !isQuoteLoading && amountInput.toDoubleOrNull()?.let { it > 0.0 } == true

    val canRefreshStatus: Boolean
        get() = hasSession && !isCheckingStatus

    val canOpenReceiveMoney: Boolean
        get() = hasSession &&
            activeSessionProvider == AddMoneyProvider.FLUTTERWAVE &&
            activeSessionMethod == FxPaymentMethod.ACCOUNT_TRANSFER &&
            when (sessionStatus) {
                AddMoneySessionStatus.CREATED,
                AddMoneySessionStatus.PENDING -> true
                else -> false
            }
}
