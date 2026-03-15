package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountActionRow(
    isRefreshing: Boolean,
    isProvisioning: Boolean,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        OutlinedButton(
            text = stringResource(R.string.funding_account_action_copy_account_number),
            onClick = onCopyAccountNumber,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRefreshing && !isProvisioning
        )
        OutlinedButton(
            text = stringResource(R.string.funding_account_action_share_details),
            onClick = onShareDetails,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRefreshing && !isProvisioning
        )

        OutlinedButton(
            text = stringResource(R.string.funding_account_action_refresh),
            onClick = onRefresh,
            isLoading = isRefreshing,
            enabled = !isProvisioning,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
