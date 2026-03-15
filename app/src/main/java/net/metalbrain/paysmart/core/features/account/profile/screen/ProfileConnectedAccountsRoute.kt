package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.fundingaccount.util.copyFundingAccountNumber
import net.metalbrain.paysmart.core.features.fundingaccount.util.shareFundingAccountDetails
import net.metalbrain.paysmart.core.features.fundingaccount.viewmodel.FundingAccountViewModel


@Composable
fun ProfileConnectedAccountsRoute(
    viewModel: FundingAccountViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ProfileConnectedAccountsScreen(
        fundingAccountState = state,
        onBack = onBack,
        onRefreshBankAccounts = viewModel::refresh,
        onProvisionBankAccount = viewModel::provision,
        onCopyBankAccountNumber = {
            state.account?.let { account ->
                copyFundingAccountNumber(context, account.accountNumber)
            }
        },
        onShareBankAccount = {
            state.account?.let { account ->
                shareFundingAccountDetails(
                    context = context,
                    account = account,
                    countryName = state.countryName,
                    countryFlagEmoji = state.countryFlagEmoji
                )
            }
        }
    )
}
