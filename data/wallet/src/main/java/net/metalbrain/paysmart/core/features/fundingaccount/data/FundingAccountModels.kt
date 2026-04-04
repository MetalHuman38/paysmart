package net.metalbrain.paysmart.core.features.fundingaccount.data

enum class FundingAccountStatus(val wireValue: String) {
    ACTIVE("active"),
    PENDING("pending"),
    DISABLED("disabled"),
    FAILED("failed");

    companion object {
        fun fromRaw(raw: String?): FundingAccountStatus {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.wireValue == normalized } ?: FAILED
        }
    }
}

enum class FundingAccountProvisioningResult(val wireValue: String) {
    CREATED("created"),
    EXISTING("existing");

    companion object {
        fun fromRaw(raw: String?): FundingAccountProvisioningResult {
            val normalized = raw?.trim()?.lowercase().orEmpty()
            return entries.firstOrNull { it.wireValue == normalized } ?: EXISTING
        }
    }
}

data class FundingAccountKyc(
    val bvn: String? = null,
    val nin: String? = null
)

data class FundingAccountData(
    val accountId: String,
    val provider: String,
    val currency: String,
    val accountNumber: String,
    val bankName: String,
    val accountName: String,
    val reference: String,
    val status: FundingAccountStatus,
    val providerStatus: String,
    val customerId: String,
    val note: String? = null,
    val createdAtMs: Long,
    val updatedAtMs: Long
)

data class FundingAccountProvisionResult(
    val account: FundingAccountData,
    val provisioningResult: FundingAccountProvisioningResult
)
