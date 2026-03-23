package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.sm),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        FundingAccountHeroCard(
            flagEmoji = state.countryFlagEmoji,
            currencyCode = state.currencyCode,
            countryName = state.countryName,
            provider = state.provider,
            isMarketSupported = state.isMarketSupported
        )

        FundingAccountStateCard(
            state = state,
            onRefresh = onRefresh,
            onProvision = onProvision
        )

        if (state.showDetails) {
            val account = state.account ?: return@Column
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
