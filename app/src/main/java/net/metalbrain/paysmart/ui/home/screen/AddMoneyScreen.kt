package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.unit.dp
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.home.viewmodel.AddMoneyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoneyScreen(
    onBack: () -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> viewModel.onPaymentSheetCompleted()
            is PaymentSheetResult.Canceled -> viewModel.onPaymentSheetCanceled()
            is PaymentSheetResult.Failed -> viewModel.onPaymentSheetFailed(result.error.localizedMessage)
        }
    }

    LaunchedEffect(uiState.paymentSheetLaunch) {
        val launch = uiState.paymentSheetLaunch ?: return@LaunchedEffect
        runCatching {
            PaymentConfiguration.init(context, launch.publishableKey)
            paymentSheet.presentWithPaymentIntent(
                launch.paymentIntentClientSecret,
                PaymentSheet.Configuration(
                    merchantDisplayName = "PaySmart"
                )
            )
        }.onFailure { error ->
            viewModel.onPaymentSheetFailed(error.localizedMessage)
        }
        viewModel.consumePaymentSheetLaunch()
    }

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.add_money_subtitle),
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = uiState.amountInput,
                onValueChange = viewModel::onAmountInputChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.add_money_amount_label)) },
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            )

            OutlinedTextField(
                value = uiState.currency,
                onValueChange = viewModel::onCurrencyChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.add_money_currency_label)) },
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            )

            OutlinedTextField(
                value = uiState.quoteTargetCurrency,
                onValueChange = viewModel::onQuoteTargetCurrencyChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Recipient currency") },
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            )

            OutlinedButton(
                onClick = viewModel::rotateQuoteMethod,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
            ) {
                Text("Payment method: ${uiState.quoteMethod.label}")
            }

            Button(
                onClick = viewModel::refreshQuote,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canRefreshQuote && !uiState.isSubmitting && !uiState.isCheckingStatus
            ) {
                if (uiState.isQuoteLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text("Refresh live FX quote")
            }

            uiState.quote?.let { quote ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription =
                                "Live quote from ${quote.sourceCurrency} to ${quote.targetCurrency}"
                        },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Rate: 1 ${quote.sourceCurrency} = ${quote.rate} ${quote.targetCurrency}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Recipient gets: ${quote.recipientAmount} ${quote.targetCurrency}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        quote.fees.forEach { fee ->
                            Text(
                                text = "${fee.label}: ${fee.amount} ${quote.sourceCurrency}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Source: ${uiState.quoteDataSource?.name ?: quote.rateSource}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.quoteError?.takeIf { it.isNotBlank() }?.let { quoteError ->
                Text(
                    text = quoteError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.createPaymentSession() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSubmit && !uiState.isCheckingStatus
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(stringResource(R.string.add_money_payment_sheet_action))
            }

            Button(
                onClick = { viewModel.refreshSessionStatus() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canRefreshStatus && !uiState.isSubmitting
            ) {
                if (uiState.isCheckingStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = 8.dp),
                        strokeWidth = 2.dp
                    )
                }
                Text(stringResource(R.string.add_money_refresh_action))
            }

            if (uiState.sessionId != null) {
                Text(
                    text = stringResource(R.string.add_money_session_id, uiState.sessionId ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            uiState.sessionStatus?.let { status ->
                Text(
                    text = stringResource(R.string.add_money_status_value, status.name.lowercase()),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            uiState.infoMessage?.takeIf { it.isNotBlank() }?.let { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.error?.takeIf { it.isNotBlank() }?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

