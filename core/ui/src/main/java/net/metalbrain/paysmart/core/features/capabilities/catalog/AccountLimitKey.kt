package net.metalbrain.paysmart.core.features.capabilities.catalog

enum class AccountLimitKey {
    SEND,
    RECEIVE,
    HOLD,
    CONVERT;

    companion object {
        fun fromRawOrNull(raw: String?): AccountLimitKey? {
            return when (raw?.trim()?.lowercase()) {
                "send" -> SEND
                "receive" -> RECEIVE
                "hold" -> HOLD
                "convert" -> CONVERT
                else -> null
            }
        }

        fun fromRaw(raw: String?): AccountLimitKey {
            return fromRawOrNull(raw)
                ?: throw IllegalArgumentException("Unknown account limit key: $raw")
        }
    }
}
