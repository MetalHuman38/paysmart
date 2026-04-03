package net.metalbrain.paysmart.core.features.fx.data

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

    companion object {
        fun fromApiCode(raw: String?): FxPaymentMethod {
            return entries.firstOrNull { it.apiCode == raw } ?: WIRE
        }
    }
}
