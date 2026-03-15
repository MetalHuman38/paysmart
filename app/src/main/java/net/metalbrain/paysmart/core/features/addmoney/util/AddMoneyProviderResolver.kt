package net.metalbrain.paysmart.core.features.addmoney.util

import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider

fun resolvePreferredAddMoneyProvider(
    availableProviders: List<AddMoneyProvider>
): AddMoneyProvider? {
    return availableProviders.firstOrNull()
}
