package net.metalbrain.paysmart.core.features.addmoney.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.card.AddMoneyAvailabilityCard
import net.metalbrain.paysmart.core.features.addmoney.card.AddMoneyErrorCard
import net.metalbrain.paysmart.core.features.addmoney.card.AddMoneySessionStatusCard
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiState
import net.metalbrain.paysmart.core.features.addmoney.util.shouldShowStandaloneSessionInfo
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.components.OutlinedButton as AppOutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
internal fun AddMoneyActionSection(
    uiState: AddMoneyUiState,
    activeProvider: AddMoneyProvider?,
    onCreatePaymentSession: () -> Unit,
    onOpenProviderCheckout: () -> Unit,
    onOpenReceiveMoney: () -> Unit,
    onRefreshSessionStatus: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Dimens.md)
    ) {
        if (!uiState.hasAvailableProvider && !uiState.hasSession) {
            AddMoneyAvailabilityCard(countryName = uiState.countryName)
        } else if (activeProvider != null) {
            PrimaryButton(
                onClick = onCreatePaymentSession,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSubmit && !uiState.isCheckingStatus,
                isLoading = uiState.isSubmitting,
                text = if (activeProvider == AddMoneyProvider.FLUTTERWAVE) {
                    stringResource(R.string.add_money_create_flutterwave_action)
                } else {
                    stringResource(R.string.add_money_payment_sheet_action)
                }
            )
        }

        if (!uiState.providerActionUrl.isNullOrBlank()) {
            PrimaryButton(
                text = stringResource(R.string.add_money_open_provider_checkout_action),
                onClick = onOpenProviderCheckout,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            )
        }

        if (uiState.canOpenReceiveMoney) {
            AppOutlinedButton(
                text = stringResource(R.string.funding_account_title),
                onClick = onOpenReceiveMoney,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            )
        }

        if (uiState.hasSession) {
            AppOutlinedButton(
                text = stringResource(R.string.add_money_refresh_action),
                onClick = onRefreshSessionStatus,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canRefreshStatus && !uiState.isSubmitting,
                isLoading = uiState.isCheckingStatus
            )
        }

        uiState.error?.let { error ->
            AddMoneyErrorCard(error = error)
        }

        AddMoneySessionStatusCard(
            sessionId = uiState.sessionId,
            sessionStatus = uiState.sessionStatus,
            provider = uiState.activeSessionProvider
        )

        if (shouldShowStandaloneSessionInfo(
                status = uiState.sessionStatus,
                provider = uiState.activeSessionProvider,
                infoMessage = uiState.infoMessage
            )
        ) {
            Text(
                text = uiState.infoMessage.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
