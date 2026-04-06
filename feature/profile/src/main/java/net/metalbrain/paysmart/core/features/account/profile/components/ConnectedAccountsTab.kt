package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.annotation.StringRes
import net.metalbrain.paysmart.feature.profile.R

enum class ConnectedAccountsTab(
    @param:StringRes val labelRes: Int,
    @param:StringRes val supportingRes: Int,
    @param:StringRes val providerRes: Int
) {
    BANK_ACCOUNTS(
        labelRes = R.string.profile_connected_accounts_tab_bank_accounts,
        supportingRes = R.string.profile_connected_accounts_bank_supporting,
        providerRes = R.string.add_money_provider_flutterwave
    ),
    CARDS(
        labelRes = R.string.profile_connected_accounts_tab_cards,
        supportingRes = R.string.profile_connected_accounts_cards_supporting,
        providerRes = R.string.add_money_provider_stripe
    )
}
