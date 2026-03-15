package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedFundingAccountCard
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsStateCard
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountScreenPhase
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun BankAccountsTabContent(
    state: FundingAccountUiState,
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        ConnectedAccountsSectionHeader(
            tab = ConnectedAccountsTab.BANK_ACCOUNTS
        )

        when (state.phase) {
            FundingAccountScreenPhase.LOADING -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.manageacct,
                    title = stringResource(R.string.funding_account_state_loading_title),
                    supporting = stringResource(R.string.funding_account_state_loading_supporting)
                )
            }

            FundingAccountScreenPhase.EMPTY -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.manageacct,
                    title = stringResource(R.string.funding_account_state_empty_title),
                    supporting = stringResource(R.string.funding_account_state_empty_supporting),
                    actionText = stringResource(R.string.funding_account_action_provision),
                    onAction = if (state.canProvision) onProvision else null
                )
            }

            FundingAccountScreenPhase.UNSUPPORTED_MARKET -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.manageacct,
                    title = stringResource(R.string.funding_account_state_unsupported_title),
                    supporting = stringResource(
                        R.string.funding_account_state_unsupported_supporting_format,
                        state.countryName
                    )
                )
            }

            FundingAccountScreenPhase.KYC_REQUIRED -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.manageacct,
                    title = stringResource(R.string.funding_account_state_kyc_required_title),
                    supporting = stringResource(R.string.funding_account_state_kyc_required_supporting)
                )
            }

            FundingAccountScreenPhase.ERROR -> {
                ConnectedAccountsStateCard(
                    animationRes = R.raw.manageacct,
                    title = stringResource(R.string.funding_account_state_error_title),
                    supporting = stringResource(R.string.funding_account_state_error_supporting),
                    actionText = stringResource(
                        if (state.canProvision) {
                            R.string.funding_account_action_retry
                        } else {
                            R.string.funding_account_action_refresh
                        }
                    ),
                    onAction = if (state.canProvision) onProvision else onRefresh
                )
            }

            FundingAccountScreenPhase.READY,
            FundingAccountScreenPhase.PENDING -> {
                val account = state.account
                if (account != null) {
                    ConnectedAccountsPhaseBanner(
                        title = stringResource(
                            if (state.phase == FundingAccountScreenPhase.READY) {
                                R.string.funding_account_state_ready_title
                            } else {
                                R.string.funding_account_state_pending_title
                            }
                        ),
                        supporting = stringResource(
                            if (state.phase == FundingAccountScreenPhase.READY) {
                                R.string.funding_account_state_ready_supporting
                            } else {
                                R.string.funding_account_state_pending_supporting
                            }
                        )
                    )
                    ConnectedFundingAccountCard(account = account)
                    ConnectedBankAccountActions(
                        isRefreshing = state.isRefreshing,
                        isProvisioning = state.isProvisioning,
                        onCopyAccountNumber = onCopyAccountNumber,
                        onShareDetails = onShareDetails,
                        onRefresh = onRefresh
                    )
                }
            }
        }
    }
}
