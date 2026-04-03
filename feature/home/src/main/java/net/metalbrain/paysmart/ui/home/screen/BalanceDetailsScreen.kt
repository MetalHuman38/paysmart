package net.metalbrain.paysmart.ui.home.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import net.metalbrain.paysmart.feature.home.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.ui.home.state.BalanceDetailsUiState
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceDetailsScreen(
    state: BalanceDetailsUiState,
    initialTab: Screen.BalanceDetails.Tab = Screen.BalanceDetails.Tab.TRANSACTIONS,
    onBack: () -> Unit,
    onViewAccountLimitsClick: (String) -> Unit = {},
    onSendClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onConvertClick: () -> Unit = {},
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val context = LocalContext.current
    val normalizedCurrency = state.currencyCode.trim().uppercase(Locale.US).ifBlank {
        CountryCapabilityCatalog.defaultProfile().currencyCode
    }
    val flag = CurrencyFlagResolver.resolve(context, normalizedCurrency)
    var selectedTabIndex by rememberSaveable(initialTab.routeValue) {
        mutableIntStateOf(
            if (initialTab == Screen.BalanceDetails.Tab.ACCOUNT_DETAILS) 1 else 0
        )
    }
    val orderedBalances = state.balancesByCurrency.entries
        .sortedWith(
            compareByDescending<Map.Entry<String, Double>> { entry ->
                entry.key.equals(normalizedCurrency, ignoreCase = true)
            }.thenBy { entry ->
                entry.key.uppercase(Locale.US)
            }
        )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = flag,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(id = R.string.home_balance_currency_title, normalizedCurrency),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(
                    id = R.string.home_balance_amount_value,
                    formatAmount(state.amount),
                    normalizedCurrency
                ),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            androidx.compose.material3.Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = { onViewAccountLimitsClick(normalizedCurrency) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.home_view_account_limits),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = Dimens.sm)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BalanceQuickAction(
                    icon = Icons.Filled.NorthEast,
                    label = stringResource(id = R.string.home_quick_action_send),
                    onClick = onSendClick
                )
                BalanceQuickAction(
                    icon = Icons.Filled.Add,
                    label = stringResource(id = R.string.home_quick_action_add),
                    onClick = onAddClick
                )
                BalanceQuickAction(
                    icon = Icons.Filled.Remove,
                    label = stringResource(id = R.string.home_quick_action_withdraw),
                    onClick = onWithdrawClick
                )
                BalanceQuickAction(
                    icon = Icons.Filled.Autorenew,
                    label = stringResource(id = R.string.home_quick_action_convert),
                    onClick = onConvertClick
                )
            }

            HomeDetailSectionCard(tonal = true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.xs),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    SegmentChip(
                        label = stringResource(id = R.string.home_segment_transactions),
                        selected = selectedTabIndex == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTabIndex = 0 }
                    )
                    SegmentChip(
                        label = stringResource(id = R.string.home_segment_account_details),
                        selected = selectedTabIndex == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTabIndex = 1 }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                HomeDetailSectionTitle(text = stringResource(id = R.string.home_recent_activity_title))

                if (state.recentTransactions.isEmpty()) {
                    HomeDetailSectionCard {
                        HomeDetailEmptyText(
                            text = stringResource(id = R.string.home_balance_no_transactions)
                        )
                    }
                } else {
                    HomeDetailSectionCard {
                        state.recentTransactions.forEachIndexed { index, transaction ->
                            BalanceActivityRow(
                                title = transaction.title,
                                subtitle = "${transaction.status} • ${transaction.date}, ${transaction.time}",
                                amount = transaction.toSignedAmountLabel(),
                                onClick = { onTransactionClick(transaction) }
                            )
                            if (index < state.recentTransactions.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            } else {
                HomeDetailSectionTitle(
                    text = stringResource(id = R.string.home_segment_account_details)
                )

                val activeTopup = state.activeFlutterwaveTopup
                if (state.accountDetailsLoading && activeTopup == null) {
                    BalanceTransferAccountLoadingCard()
                } else if (activeTopup?.virtualAccount != null) {
                    BalanceTransferAccountDetailsSection(
                        session = activeTopup,
                        isLoading = state.accountDetailsLoading,
                        onCopyField = { label, value ->
                            copyBalanceTransferField(
                                context = context,
                                label = label,
                                value = value
                            )
                        },
                        onShareDetails = {
                            shareBalanceTransferDetails(
                                context = context,
                                session = activeTopup
                            )
                        }
                    )
                } else if (normalizedCurrency.equals("GBP", ignoreCase = true)) {
                    UkDomesticAccountDetailsSection()
                } else {
                    HomeDetailSectionCard {
                        if (orderedBalances.isEmpty()) {
                            HomeDetailEmptyText(
                                text = stringResource(id = R.string.home_balance_no_account_data)
                            )
                        } else {
                            orderedBalances.forEachIndexed { index, entry ->
                                BalanceDetailLine(
                                    label = entry.key.uppercase(Locale.US),
                                    value = formatCurrencyAmount(entry.value, entry.key)
                                )
                                if (index < orderedBalances.lastIndex || state.walletUpdatedAtMs != null) {
                                    HorizontalDivider()
                                }
                            }

                            state.walletUpdatedAtMs?.let { updatedAtMs ->
                                BalanceDetailLine(
                                    label = stringResource(id = R.string.home_balance_last_synced),
                                    value = updatedAtMs.toBalanceDateTimeLabel()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyBalanceTransferField(
    context: android.content.Context,
    label: String,
    value: String
) {
    if (value.isBlank()) return

    context.getSystemService<ClipboardManager>()
        ?.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(
        context,
        context.getString(R.string.home_balance_transfer_copy_success_format, label),
        Toast.LENGTH_SHORT
    ).show()
}

private fun shareBalanceTransferDetails(
    context: android.content.Context,
    session: net.metalbrain.paysmart.core.features.addmoney.data.AddMoneySessionData
) {
    val virtualAccount = session.virtualAccount ?: return
    val shareText = buildString {
        appendLine(context.getString(R.string.home_balance_transfer_share_title))
        virtualAccount.accountName?.takeIf { it.isNotBlank() }?.let { accountName ->
            appendLine("${context.getString(R.string.home_balance_transfer_account_holder_label)}: $accountName")
        }
        appendLine("${context.getString(R.string.funding_account_details_bank_name)}: ${virtualAccount.bankName}")
        appendLine("${context.getString(R.string.funding_account_details_account_number)}: ${virtualAccount.accountNumber}")
        appendLine("${context.getString(R.string.home_balance_transfer_exact_amount_label)}: ${formatMinorAmountForShare(session.amountMinor, session.currency)}")
        appendLine("${context.getString(R.string.funding_account_details_reference)}: ${virtualAccount.reference}")
        appendLine("${context.getString(R.string.home_balance_transfer_expires_label)}: ${session.expiresAtMs.toBalanceDateTimeLabel()}")
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.home_balance_transfer_share_title))
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(
        Intent.createChooser(
            shareIntent,
            context.getString(R.string.funding_account_action_share_details)
        )
    )
}

private fun formatMinorAmountForShare(amountMinor: Int, currencyCode: String): String {
    return String.format(Locale.US, "%.2f %s", amountMinor.toDouble() / 100.0, currencyCode.uppercase(Locale.US))
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}

private fun formatCurrencyAmount(amount: Double, currencyCode: String): String {
    return String.format(Locale.US, "%.2f %s", amount, currencyCode.uppercase(Locale.US))
}

private fun Transaction.toSignedAmountLabel(): String {
    val prefix = if (amount > 0) "+" else ""
    return prefix + String.format(Locale.US, "%.2f %s", amount, currency)
}

private val BALANCE_UPDATED_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

private fun Long.toBalanceDateTimeLabel(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(BALANCE_UPDATED_FORMAT)
}
