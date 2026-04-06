package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ConnectedBankAccountActions(
    isRefreshing: Boolean,
    isProvisioning: Boolean,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        OutlinedButton(
            text = stringResource(R.string.funding_account_action_copy_account_number),
            onClick = onCopyAccountNumber,
            enabled = !isRefreshing && !isProvisioning
        )
        OutlinedButton(
            text = stringResource(R.string.funding_account_action_share_details),
            onClick = onShareDetails,
            enabled = !isRefreshing && !isProvisioning
        )
        OutlinedButton(
            text = stringResource(R.string.funding_account_action_refresh),
            onClick = onRefresh,
            isLoading = isRefreshing,
            enabled = !isProvisioning,
            loadingText = stringResource(R.string.funding_account_action_refresh)
        )
    }
}
