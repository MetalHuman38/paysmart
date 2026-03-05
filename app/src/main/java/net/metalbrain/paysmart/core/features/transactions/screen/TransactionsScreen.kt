package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.sheets.CurrencyFilterSheet
import net.metalbrain.paysmart.core.features.account.sheets.StatusFilterSheet
import net.metalbrain.paysmart.core.features.transactions.components.TransactionFilter
import net.metalbrain.paysmart.core.features.transactions.components.TransactionList
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionsViewModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.components.FilterTabs
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val filterSheetState = rememberModalBottomSheetState()
    val detailSheetState = rememberModalBottomSheetState()
    var activeFilter by remember { mutableStateOf<TransactionFilter>(TransactionFilter.All) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.transactions))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState
        ) {
            when (activeFilter) {
                is TransactionFilter.Status -> StatusFilterSheet(
                    selected = viewModel.selectedStatus,
                    onSelect = {
                        viewModel.setStatusFilter(it)
                        showFilterSheet = false
                    }
                )

                is TransactionFilter.Currency -> CurrencyFilterSheet(
                    selected = viewModel.selectedCurrencies,
                    onSelect = {
                        viewModel.setCurrencyFilter(it)
                        showFilterSheet = false
                    }
                )

                else -> Unit
            }
        }
    }

    selectedTransaction?.let { transaction ->
        ModalBottomSheet(
            onDismissRequest = { selectedTransaction = null },
            sheetState = detailSheetState
        ) {
            TransactionDetailsSheet(transaction = transaction)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            HomeBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.transactions_title),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            FilterTabs(
                current = activeFilter,
                onTabClick = {
                    if (it == TransactionFilter.All) {
                        viewModel.clearFilters()
                        activeFilter = TransactionFilter.All
                        showFilterSheet = false
                    } else {
                        activeFilter = it
                        showFilterSheet = true
                    }
                }
            )

            if (transactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    Text(
                        text = stringResource(R.string.transactions_empty_subtitle),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                TransactionList(
                    transactions = transactions,
                    onTransactionClick = {
                        showFilterSheet = false
                        selectedTransaction = it
                    }
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailsSheet(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.transaction_details_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        TransactionDetailRow(
            label = stringResource(R.string.transaction_details_reference),
            value = transaction.id
        )
        TransactionDetailRow(
            label = stringResource(R.string.transaction_details_status),
            value = transaction.status
        )
        TransactionDetailRow(
            label = stringResource(R.string.transaction_details_amount),
            value = stringResource(
                R.string.transaction_amount_format,
                String.format(Locale.US, "%.2f", transaction.amount),
                transaction.currency
            )
        )
        TransactionDetailRow(
            label = stringResource(R.string.transaction_details_date),
            value = transaction.date
        )
        TransactionDetailRow(
            label = stringResource(R.string.transaction_details_time),
            value = transaction.time
        )
    }
}

@Composable
private fun TransactionDetailRow(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
