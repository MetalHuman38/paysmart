package net.metalbrain.paysmart.ui.transactions.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.ui.components.FilterTabs
import net.metalbrain.paysmart.ui.transactions.components.TransactionFilter
import net.metalbrain.paysmart.ui.transactions.components.TransactionList
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation
import net.metalbrain.paysmart.ui.sheets.CurrencyFilterSheet
import net.metalbrain.paysmart.ui.sheets.StatusFilterSheet
import net.metalbrain.paysmart.ui.transactions.viewmodel.TransactionsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var activeFilter by remember { mutableStateOf<TransactionFilter>(TransactionFilter.All) }
    var showSheet by remember { mutableStateOf(false) }

    // Bottom Sheet Launcher
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            when (activeFilter) {
                is TransactionFilter.Status -> StatusFilterSheet(
                    selected = viewModel.selectedStatus,
                    onSelect = {
                        viewModel.setStatusFilter(it)
                        showSheet = false
                    }
                )

                is TransactionFilter.Currency -> CurrencyFilterSheet(
                    selected = viewModel.selectedCurrencies,
                    onSelect = {
                        viewModel.setCurrencyFilter(it)
                        showSheet = false
                    }
                )

                else -> {}
            }
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
                .padding(innerPadding) // ✅ Handles top + bottom insets
        ) {

            // Transaction Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            FilterTabs(
                current = activeFilter,
                onTabClick = {
                    if (it == TransactionFilter.All) {
                        viewModel.clearFilters()
                    } else {
                        activeFilter = it
                        showSheet = true
                    }
                }
            )

            TransactionList(transactions = transactions)
        }
    }
}
