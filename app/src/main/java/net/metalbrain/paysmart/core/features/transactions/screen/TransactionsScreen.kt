package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.transactions.components.TransactionFilter
import net.metalbrain.paysmart.core.features.transactions.components.TransactionFilterTabs
import net.metalbrain.paysmart.core.features.transactions.components.TransactionList
import net.metalbrain.paysmart.core.features.transactions.components.TransactionsEmptyState
import net.metalbrain.paysmart.core.features.transactions.components.TransactionsHeader
import net.metalbrain.paysmart.core.features.transactions.sheet.TransactionDetailsSheet
import net.metalbrain.paysmart.core.features.transactions.sheet.TransactionFilterSheet
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionsViewModel
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val availableStatuses by viewModel.availableStatuses.collectAsState()
    val availableCurrencies by viewModel.availableCurrencies.collectAsState()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var activeFilter by remember { mutableStateOf<TransactionFilter>(TransactionFilter.All) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = filterSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            when (activeFilter) {
                is TransactionFilter.Status -> TransactionFilterSheet(
                    title = stringResource(R.string.sheet_select_status_title),
                    options = availableStatuses,
                    selected = viewModel.selectedStatus,
                    onApply = {
                        viewModel.setStatusFilter(it)
                        showFilterSheet = false
                    }
                )

                is TransactionFilter.Currency -> TransactionFilterSheet(
                    title = stringResource(R.string.sheet_select_currency_title),
                    options = availableCurrencies,
                    selected = viewModel.selectedCurrencies,
                    onApply = {
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
            sheetState = detailSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            TransactionDetailsSheet(transaction = transaction)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { HomeBottomNavigation(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TransactionsHeader()

            TransactionFilterTabs(
                current = activeFilter,
                onTabClick = { filter ->
                    if (filter == TransactionFilter.All) {
                        viewModel.clearFilters()
                        activeFilter = TransactionFilter.All
                        showFilterSheet = false
                    } else {
                        activeFilter = filter
                        showFilterSheet = true
                    }
                }
            )

            if (transactions.isEmpty()) {
                TransactionsEmptyState()
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
