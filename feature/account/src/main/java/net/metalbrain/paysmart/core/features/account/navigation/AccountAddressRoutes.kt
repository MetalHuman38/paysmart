package net.metalbrain.paysmart.core.features.account.navigation

import androidx.navigation.NavGraphBuilder

fun NavGraphBuilder.accountAddressRoutes(
    onAddressComplete: () -> Unit,
    onAddressBack: () -> Unit,
) {
    // AddressSetupResolverScreen migrates here once it moves from :app to :feature:account.
}
