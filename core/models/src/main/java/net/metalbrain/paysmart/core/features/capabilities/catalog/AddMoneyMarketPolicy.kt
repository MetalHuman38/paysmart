package net.metalbrain.paysmart.core.features.capabilities.catalog

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod

data class AddMoneyMarketPolicy(
    val providers: List<AddMoneyProvider>,
    val methods: List<FxPaymentMethod>
) {
    val isSupported: Boolean
        get() = providers.isNotEmpty()

    val preferredProvider: AddMoneyProvider?
        get() = providers.firstOrNull()
}
