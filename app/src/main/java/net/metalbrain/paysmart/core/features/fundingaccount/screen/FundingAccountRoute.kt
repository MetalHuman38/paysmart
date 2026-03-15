package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.core.features.fundingaccount.util.copyFundingAccountNumber
import net.metalbrain.paysmart.core.features.fundingaccount.util.shareFundingAccountDetails
import net.metalbrain.paysmart.core.features.fundingaccount.viewmodel.FundingAccountViewModel

@Composable
fun FundingAccountRoute(
    viewModel: FundingAccountViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    FundingAccountScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onProvision = viewModel::provision,
        onCopyAccountNumber = {
            state.account?.let { account ->
                copyFundingAccountNumber(context, account.accountNumber)
            }
        },
        onShareDetails = {
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
