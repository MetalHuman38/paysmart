package net.metalbrain.paysmart.core.features.capabilities.catalog

enum class CapabilityKey {
    SEND_INTERNATIONAL,
    CARD_SPEND_ABROAD,
    HOLD_AND_CONVERT,
    RECEIVE_MONEY,
    EARN_RETURN;

    companion object {
        fun fromRaw(raw: String?): CapabilityKey {
            return when (raw?.trim()?.lowercase()) {
                "send_international" -> SEND_INTERNATIONAL
                "card_spend_abroad" -> CARD_SPEND_ABROAD
                "hold_and_convert" -> HOLD_AND_CONVERT
                "receive_money" -> RECEIVE_MONEY
                "earn_return" -> EARN_RETURN
                else -> SEND_INTERNATIONAL
            }
        }
    }
}
