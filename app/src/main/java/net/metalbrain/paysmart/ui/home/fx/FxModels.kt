package net.metalbrain.paysmart.ui.home.fx

enum class FxPaymentMethod(val apiCode: String, val label: String) {
    WIRE("wire", "Wire transfer"),
    DEBIT_CARD("debitCard", "Debit card"),
    CREDIT_CARD("creditCard", "Credit card"),
    ACCOUNT_TRANSFER("accountTransfer", "Account transfer");

    fun next(): FxPaymentMethod {
        val all = entries
        val index = all.indexOf(this)
        return all[(index + 1) % all.size]
    }
}

data class FxFeeLine(
    val label: String,
    val amount: Double,
    val code: String? = null
)

data class FxQuote(
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val rate: Double,
    val recipientAmount: Double,
    val fees: List<FxFeeLine>,
    val guaranteeSeconds: Int,
    val arrivalSeconds: Int,
    val rateSource: String,
    val updatedAtMs: Long
)

data class FxQuoteQuery(
    val sourceCurrency: String,
    val targetCurrency: String,
    val sourceAmount: Double,
    val method: FxPaymentMethod
)

enum class FxQuoteDataSource {
    SERVER,
    CACHE
}

data class FxQuoteResult(
    val quote: FxQuote,
    val dataSource: FxQuoteDataSource
)

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
