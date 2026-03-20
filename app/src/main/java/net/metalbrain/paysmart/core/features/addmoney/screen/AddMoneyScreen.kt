package net.metalbrain.paysmart.core.features.addmoney.screen

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheet.CustomerConfiguration
import com.stripe.android.paymentsheet.PaymentSheetResult
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.addmoney.util.resolvePreferredAddMoneyProvider
import net.metalbrain.paysmart.core.features.addmoney.viewmodel.AddMoneyViewModel
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.ui.components.CatalogSelectionBottomSheet
import net.metalbrain.paysmart.ui.components.CatalogSelectionOption
import java.util.Locale

@Composable
fun AddMoneyScreen(
    onBack: () -> Unit,
    onOpenAccountDetails: (String) -> Unit,
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
    val selectedCurrency = remember(context, uiState.currency) {
        CountrySelectionCatalog.currencyByCode(context, uiState.currency)
    }
    val activeProvider = uiState.activeSessionProvider
        ?: resolvePreferredAddMoneyProvider(uiState.availableAddMoneyProviders)
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
                    merchantDisplayName = "PaySmart",
                    customer = launch.customer?.let { customer ->
                        CustomerConfiguration(
                            id = customer.customerId,
                            ephemeralKeySecret = customer.ephemeralKeySecret
                        )
                    }
                )
            )
        }.onFailure { error ->
            viewModel.onPaymentSheetFailed(error.localizedMessage)
        }
        viewModel.consumePaymentSheetLaunch()
    }

    AddMoneyContent(
        uiState = uiState,
        currencyDisplayName = selectedCurrency?.displayName ?: uiState.currency,
        currencyFlagEmoji = currencyFlagEmoji,
        activeProvider = activeProvider,
        onBack = onBack,
        onCurrencyClick = { showCurrencySheet = true },
        onAmountChange = viewModel::onAmountInputChanged,
        onQuoteMethodChange = viewModel::onQuoteMethodChanged,
        onRefreshQuote = viewModel::refreshQuote,
        onCreatePaymentSession = viewModel::createPaymentSession,
        onRefreshSessionStatus = viewModel::refreshSessionStatus,
        onOpenAccountDetails = {
            val currencyCode = uiState.activeSessionCurrency ?: uiState.currency
            onOpenAccountDetails(currencyCode)
        },
        onOpenProviderCheckout = {
            uiState.providerActionUrl?.let { providerActionUrl ->
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, providerActionUrl.toUri())
                    context.startActivity(intent)
                }.onFailure { error ->
                    viewModel.onPaymentSheetFailed(error.localizedMessage)
                }
            }
        }
    )

    if (showCurrencySheet) {
        CatalogSelectionBottomSheet(
            title = stringResource(R.string.add_money_select_currency_title),
            options = currencyOptions,
            selectedKey = uiState.currency.uppercase(Locale.US),
            onDismiss = { showCurrencySheet = false },
            onSelect = { selected ->
                viewModel.onCurrencyChanged(selected.key)
                showCurrencySheet = false
            }
        )
    }
}
