package net.metalbrain.paysmart.core.features.addmoney.screen

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.core.net.toUri
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.data.AddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.viewmodel.AddMoneyViewModel
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.ui.components.CatalogSelectionBottomSheet
import net.metalbrain.paysmart.ui.components.CatalogSelectionOption
import net.metalbrain.paysmart.ui.components.OutlinedButton as AppOutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMoneyScreen(
    onBack: () -> Unit,
    viewModel: AddMoneyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showCurrencySheet by remember { mutableStateOf(false) }
    val currencyOptions = remember(context) {
        CountrySelectionCatalog.currencies(context).map { currency ->
            CatalogSelectionOption(
                key = currency.code,
                title = "${currency.code} - ${currency.displayName}",
                subtitle = currency.code,
                leadingEmoji = currency.flagEmoji
            )
        }
    }
    val currencyFlagEmoji = CurrencyFlagResolver.resolve(
        context = context,
        currencyCode = uiState.currency,
        preferredCurrencyCode = uiState.countryCurrencyCode,
        preferredFlagEmoji = uiState.countryFlagEmoji
    )
    val activeProvider = uiState.activeSessionProvider ?: inferProvider(
        currency = uiState.currency,
        countryIso2 = uiState.countryIso2
    )
    val paymentResultCallback: (PaymentSheetResult) -> Unit = remember {
        { result ->
            when (result) {
                is PaymentSheetResult.Completed -> viewModel.onPaymentSheetCompleted()
                is PaymentSheetResult.Canceled -> viewModel.onPaymentSheetCanceled()
                is PaymentSheetResult.Failed ->
                    viewModel.onPaymentSheetFailed(result.error.localizedMessage)
            }
        }
    }
    val paymentSheet = PaymentSheet.Builder(paymentResultCallback).build()

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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.add_money_you_add),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.currency,
                        onValueChange = {},
                        modifier = Modifier
                            .width(130.dp)
                            .clickable(
                                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                            ) {
                                showCurrencySheet = true
                            },
                        readOnly = true,
                        singleLine = true,
                        leadingIcon = { Text(text = currencyFlagEmoji) },
                        trailingIcon = {
                            IconButton(
                                onClick = { showCurrencySheet = true },
                                enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        },
                        label = { Text(stringResource(R.string.add_money_currency_label)) },
                        enabled = !uiState.isSubmitting && !uiState.isCheckingStatus,
                        supportingText = { Text(stringResource(R.string.add_money_currency_supporting)) }
                    )
                    OutlinedTextField(
                        value = uiState.amountInput,
                        onValueChange = viewModel::onAmountInputChanged,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.End,
                            fontWeight = FontWeight.Bold
                        ),
                        label = { Text(stringResource(R.string.add_money_amount_label)) },
                        enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                    )
                }
            }

            Text(
                text = uiState.topUpPolicyHint.ifBlank {
                    stringResource(R.string.add_money_subtitle)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.quote?.let { quote ->
                        Text(
                            text = stringResource(
                                R.string.add_money_quote_rate_format,
                                quote.sourceCurrency,
                                quote.rate.toString(),
                                quote.targetCurrency
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    SummaryLine(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = stringResource(R.string.add_money_summary_paying_in),
                        value = "$currencyFlagEmoji ${uiState.currency.uppercase(Locale.US)}"
                    )

                    SummaryLine(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = stringResource(R.string.add_money_summary_paying_with),
                        value = uiState.quoteMethod.label
                    )

                    SummaryLine(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = stringResource(R.string.add_money_summary_arrives),
                        value = uiState.quote?.let {
                            if (it.arrivalSeconds <= 120) {
                                stringResource(R.string.add_money_summary_arrives_today_seconds)
                            } else {
                                stringResource(R.string.add_money_summary_arrives_today)
                            }
                        } ?: stringResource(R.string.add_money_summary_arrives_estimate)
                    )

                    uiState.quote?.let { quote ->
                        SummaryLine(
                            icon = {
                                Text(
                                    text = "\uD83E\uDDFE",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            label = stringResource(R.string.add_money_summary_you_pay),
                            value = "${quote.sourceAmount} ${quote.sourceCurrency}"
                        )

                        quote.fees.firstOrNull()?.let { fee ->
                            SummaryLine(
                                icon = {
                                    Text(
                                        text = "\u2139\uFE0F",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                label = fee.label,
                                value = "${fee.amount} ${quote.sourceCurrency}"
                            )
                        }
                    }
                }
            }

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
                        onClick = { viewModel.onQuoteMethodChanged(method) },
                        label = { Text(method.label) },
                        enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                    )
                }
            }

            AppOutlinedButton(
                text = stringResource(R.string.add_money_refresh_quote_action),
                onClick = viewModel::refreshQuote,
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

            PrimaryButton(
                onClick = { viewModel.createPaymentSession() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSubmit && !uiState.isCheckingStatus,
                isLoading = uiState.isSubmitting,
                text = if (activeProvider == AddMoneyProvider.FLUTTERWAVE) {
                    stringResource(R.string.add_money_create_flutterwave_action)
                } else {
                    stringResource(R.string.add_money_payment_sheet_action)
                }
            )

            val providerActionUrl = uiState.providerActionUrl
            if (!providerActionUrl.isNullOrBlank()) {
                PrimaryButton(
                    text = stringResource(R.string.add_money_open_provider_checkout_action),
                    onClick = {
                        runCatching {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                providerActionUrl.toUri()
                            )
                            context.startActivity(intent)
                        }.onFailure { error ->
                            viewModel.onPaymentSheetFailed(error.localizedMessage)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSubmitting && !uiState.isCheckingStatus
                )
            }

            AppOutlinedButton(
                text = stringResource(R.string.add_money_refresh_action),
                onClick = { viewModel.refreshSessionStatus() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canRefreshStatus && !uiState.isSubmitting,
                isLoading = uiState.isCheckingStatus
            )

            if (uiState.sessionId != null || uiState.sessionStatus != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        uiState.sessionId?.let { sessionId ->
                            Text(
                                text = stringResource(R.string.add_money_session_id, sessionId),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        uiState.sessionStatus?.let { status ->
                            Text(
                                text = stringResource(
                                    R.string.add_money_status_value,
                                    status.name.lowercase()
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.activeSessionProvider?.let { provider ->
                            Text(
                                text = stringResource(
                                    R.string.add_money_provider_value,
                                    when (provider) {
                                        AddMoneyProvider.STRIPE -> stringResource(
                                            R.string.add_money_provider_stripe
                                        )
                                        AddMoneyProvider.FLUTTERWAVE -> stringResource(
                                            R.string.add_money_provider_flutterwave
                                        )
                                    }
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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

    if (showCurrencySheet) {
        CatalogSelectionBottomSheet(
            title = stringResource(R.string.add_money_select_currency_title),
            options = currencyOptions,
            selectedKey = uiState.currency.uppercase(Locale.US),
            onDismiss = { showCurrencySheet = false },
            onSelect = { selected ->
                viewModel.onCurrencyChanged(selected.key)
            }
        )
    }
}

private fun inferProvider(
    currency: String,
    countryIso2: String
): AddMoneyProvider {
    val normalizedCurrency = currency.trim().uppercase(Locale.US)
    val normalizedCountry = countryIso2.trim().uppercase(Locale.US)
    return if (normalizedCurrency == "NGN" || normalizedCountry == "NG") {
        AddMoneyProvider.FLUTTERWAVE
    } else {
        AddMoneyProvider.STRIPE
    }
}

@Composable
private fun SummaryLine(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
