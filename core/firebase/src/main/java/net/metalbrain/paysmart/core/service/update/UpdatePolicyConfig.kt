package net.metalbrain.paysmart.core.service.update

data class UpdatePolicyConfig(
    val enabled: Boolean = true,
    val flexibleMinStalenessDays: Int = 2,
    val flexibleMinPriority: Int = 0,
    val immediateMinStalenessDays: Int = 7,
    val immediateMinPriority: Int = 4,
    val immediateRetryCooldownMinutes: Long = 15L,
)

interface UpdatePolicyConfigProvider {
    fun getConfig(): UpdatePolicyConfig
}
