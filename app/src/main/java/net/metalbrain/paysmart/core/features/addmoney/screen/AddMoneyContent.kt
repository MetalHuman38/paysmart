package net.metalbrain.paysmart.core.features.addmoney.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.card.AddMoneyInputCard
import net.metalbrain.paysmart.core.features.addmoney.card.AddMoneySummaryCard
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyUiState
import net.metalbrain.paysmart.core.features.fx.data.FxPaymentMethod
import net.metalbrain.paysmart.ui.components.OutlinedButton as AppOutlinedButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun AddMoneyContent(
    uiState: AddMoneyUiState,
    currencyDisplayName: String,
    currencyFlagEmoji: String,
    activeProvider: AddMoneyProvider?,
    onBack: () -> Unit,
    onCurrencyClick: () -> Unit,
    onAmountChange: (String) -> Unit,
    onQuoteMethodChange: (FxPaymentMethod) -> Unit,
    onRefreshQuote: () -> Unit,
    onCreatePaymentSession: () -> Unit,
    onOpenProviderCheckout: () -> Unit,
    onOpenAccountDetails: () -> Unit,
    onRefreshSessionStatus: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_money_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.add_money_you_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            AddMoneyInputCard(
                currency = uiState.currency,
                currencyDisplayName = currencyDisplayName,
                currencyFlagEmoji = currencyFlagEmoji,
                amountInput = uiState.amountInput,
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus,
                onCurrencyClick = onCurrencyClick,
                onAmountChange = onAmountChange
            )

            Text(
                text = uiState.topUpPolicyHint.ifBlank {
                    stringResource(R.string.add_money_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AddMoneySummaryCard(
                uiState = uiState,
                currencyFlagEmoji = currencyFlagEmoji
            )

            Text(
                text = stringResource(R.string.add_money_payment_method_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.availableMethods.forEach { method ->
                    FilterChip(
                        selected = uiState.quoteMethod == method,
                        onClick = { onQuoteMethodChange(method) },
                        label = { Text(method.label) },
                        enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                    )
                }
            }

            AppOutlinedButton(
                text = stringResource(R.string.add_money_refresh_quote_action),
                onClick = onRefreshQuote,
                enabled = uiState.canRefreshQuote && !uiState.isSubmitting && !uiState.isCheckingStatus,
                isLoading = uiState.isQuoteLoading,
                modifier = Modifier.fillMaxWidth()
            )

            uiState.quoteError?.takeIf { it.isNotBlank() }?.let { quoteError ->
                Text(
                    text = quoteError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            AddMoneyActionSection(
                uiState = uiState,
                activeProvider = activeProvider,
                onCreatePaymentSession = onCreatePaymentSession,
                onOpenProviderCheckout = onOpenProviderCheckout,
                onOpenAccountDetails = onOpenAccountDetails,
                onRefreshSessionStatus = onRefreshSessionStatus
            )
        }
    }
}
