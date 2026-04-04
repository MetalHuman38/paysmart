package net.metalbrain.paysmart.core.features.fundingaccount.data

enum class FundingAccountErrorCode(val wireValue: String) {
    FLUTTERWAVE_FUNDING_ACCOUNT_NOT_FOUND("FLUTTERWAVE_FUNDING_ACCOUNT_NOT_FOUND"),
    FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED("FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED"),
    FLUTTERWAVE_FUNDING_ACCOUNT_CONFLICT("FLUTTERWAVE_FUNDING_ACCOUNT_CONFLICT"),
    FLUTTERWAVE_PROVIDER_REJECTED("FLUTTERWAVE_PROVIDER_REJECTED"),
    MISSING_FLUTTERWAVE_SECRET_KEY("MISSING_FLUTTERWAVE_SECRET_KEY"),
    MISSING_FLUTTERWAVE_PUBLIC_KEY("MISSING_FLUTTERWAVE_PUBLIC_KEY");

    companion object {
        fun fromRaw(raw: String?): FundingAccountErrorCode? {
            val normalized = raw?.trim()?.uppercase().orEmpty()
            return entries.firstOrNull { it.wireValue == normalized }
        }
    }
}
