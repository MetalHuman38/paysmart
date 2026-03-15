package net.metalbrain.paysmart.core.features.addmoney.data

enum class AddMoneyErrorCode(val wireValue: String) {
    MISSING_STRIPE_PUBLISHABLE_KEY("MISSING_STRIPE_PUBLISHABLE_KEY"),
    MISSING_STRIPE_SECRET_KEY("MISSING_STRIPE_SECRET_KEY"),
    INVALID_STRIPE_SECRET_KEY("INVALID_STRIPE_SECRET_KEY"),
    MISSING_FLUTTERWAVE_SECRET_KEY("MISSING_FLUTTERWAVE_SECRET_KEY"),
    MISSING_FLUTTERWAVE_PUBLIC_KEY("MISSING_FLUTTERWAVE_PUBLIC_KEY"),
    FLUTTERWAVE_NOT_IMPLEMENTED("FLUTTERWAVE_NOT_IMPLEMENTED"),
    SESSION_VALIDATION_UNAVAILABLE("SESSION_VALIDATION_UNAVAILABLE");

    companion object {
        fun fromRaw(raw: String?): AddMoneyErrorCode? {
            val normalized = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.wireValue == normalized }
        }
    }
}

data class AddMoneyUiError(
    val message: String,
    val title: String? = null,
    val code: AddMoneyErrorCode? = null,
    val supportingText: String? = null,
    val isConfigurationIssue: Boolean = false
)
