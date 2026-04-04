package net.metalbrain.paysmart.core.features.cards.data

enum class ManagedCardErrorCode {
    STRIPE_MANAGED_CARDS_UNAVAILABLE,
    STRIPE_MANAGED_CARD_NOT_FOUND,
    STRIPE_MANAGED_CARD_ACTION_FAILED;

    companion object {
        fun fromStatusCode(statusCode: Int): ManagedCardErrorCode {
            return when (statusCode) {
                404 -> STRIPE_MANAGED_CARD_NOT_FOUND
                else -> STRIPE_MANAGED_CARDS_UNAVAILABLE
            }
        }
    }
}
