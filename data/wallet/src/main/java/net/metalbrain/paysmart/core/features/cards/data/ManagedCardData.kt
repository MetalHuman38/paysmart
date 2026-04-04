package net.metalbrain.paysmart.core.features.cards.data

enum class ManagedCardStatus {
    ACTIVE,
    DETACHED;

    companion object {
        fun fromRaw(raw: String?): ManagedCardStatus {
            return when (raw?.trim()?.lowercase()) {
                "detached" -> DETACHED
                else -> ACTIVE
            }
        }
    }
}

data class ManagedCardData(
    val id: String,
    val provider: String = "stripe",
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val funding: String? = null,
    val country: String? = null,
    val fingerprint: String? = null,
    val isDefault: Boolean = false,
    val status: ManagedCardStatus = ManagedCardStatus.ACTIVE,
    val createdAtMs: Long = 0L,
    val updatedAtMs: Long = 0L
)
