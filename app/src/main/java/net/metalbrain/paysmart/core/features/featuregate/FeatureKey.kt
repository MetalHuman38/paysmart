package net.metalbrain.paysmart.core.features.featuregate

enum class FeatureKey(val id: String) {
    ADD_MONEY("add_money"),

    RECEIVE_MONEY("receive_money"),
    
    SEND_MONEY("send_money"),

    CREATE_INVOICE("create_invoice");

    companion object {
        fun fromId(raw: String?): FeatureKey {
            return entries.firstOrNull { it.id == raw } ?: ADD_MONEY
        }
    }
}
