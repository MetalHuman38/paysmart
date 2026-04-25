package net.metalbrain.paysmart.core.features.account.navigation

import androidx.navigation.NavGraphBuilder

fun NavGraphBuilder.accountRecoveryRoutes(
    onRecoverBack: () -> Unit,
    onRecoverHelp: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onChangePhoneClick: () -> Unit,
    onChangePasswordBack: () -> Unit,
    onChangePasswordSuccess: () -> Unit,
    onChangePhoneBack: () -> Unit,
    onChangePhoneSuccess: () -> Unit,
) {
    // RecoverAccount, ChangePasswordRecovery, ChangePhoneRecovery routes migrate here
    // once their screens move from :app to :feature:account.
}
