package net.metalbrain.paysmart.core.features.fx.data


data class FxQuoteUiState(
    val sourceCurrency: String = "USD",
    val targetCurrency: String = "NGN",
    val amountInput: String = "100",
    val method: FxPaymentMethod = FxPaymentMethod.WIRE,
    val isLoading: Boolean = false,
    val quote: FxQuote? = null,
    val dataSource: FxQuoteDataSource? = null,
    val error: String? = null
)
