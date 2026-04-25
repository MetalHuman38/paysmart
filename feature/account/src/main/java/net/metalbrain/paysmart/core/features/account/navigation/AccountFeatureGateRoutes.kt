package net.metalbrain.paysmart.core.features.account.navigation

import androidx.navigation.NavGraphBuilder

fun NavGraphBuilder.accountFeatureGateRoutes(
    onFeatureAllowed: (resumeRoute: String) -> Unit,
    onFeatureRequiresEmail: (gateRoute: String) -> Unit,
    onFeatureRequiresAddress: () -> Unit,
    onFeatureRequiresIdentity: () -> Unit,
    onFeatureRequiresSecurity: () -> Unit,
    onFeatureGateBack: () -> Unit,
) {
    // FeatureGateScreen migrates here once it moves from :app to :feature:account.
}
