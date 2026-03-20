package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.core.features.fundingaccount.card.FundingAccountActionRow
import net.metalbrain.paysmart.core.features.fundingaccount.card.FundingAccountDetailsCard
import net.metalbrain.paysmart.core.features.fundingaccount.card.FundingAccountHeroCard
import net.metalbrain.paysmart.core.features.fundingaccount.card.FundingAccountStateCard
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountContent(
    state: FundingAccountUiState,
    contentPadding: PaddingValues,
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.mediumScreenPadding, vertical = Dimens.md),
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        FundingAccountHeroCard(
            flagEmoji = state.countryFlagEmoji,
            currencyCode = state.currencyCode,
            countryName = state.countryName,
            provider = state.provider
        )

        FundingAccountStateCard(
            state = state,
            onRefresh = onRefresh,
            onProvision = onProvision
        )

        state.account?.let { account ->
            FundingAccountActionRow(
                isRefreshing = state.isRefreshing,
                isProvisioning = state.isProvisioning,
                onCopyAccountNumber = onCopyAccountNumber,
                onShareDetails = onShareDetails,
                onRefresh = onRefresh
            )

            FundingAccountDetailsCard(account = account)
        }
    }
}
