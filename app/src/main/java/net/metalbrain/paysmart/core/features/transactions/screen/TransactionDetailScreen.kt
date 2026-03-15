package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.transactions.components.TransactionFactsCard
import net.metalbrain.paysmart.core.features.transactions.components.TransactionQuickActionsRow
import net.metalbrain.paysmart.core.features.transactions.components.TransactionUpdatesCard
import net.metalbrain.paysmart.core.features.transactions.state.TransactionDetailUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    state: TransactionDetailUiState,
    onBack: () -> Unit,
    onShareReceipt: () -> Unit,
    onCopyReference: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(TransactionDetailTab.Details) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
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

        val transaction = state.transaction
        if (transaction == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.transaction_detail_not_found),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(Dimens.space10),
            verticalArrangement = Arrangement.spacedBy(Dimens.space8)
        ) {
            item {
                TransactionDetailSummary(transaction = transaction)
            }

            item {
                TransactionDetailTabs(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                when (selectedTab) {
                    TransactionDetailTab.Updates -> TransactionUpdatesCard(transaction = transaction)
                    TransactionDetailTab.Details -> TransactionFactsCard(transaction = transaction)
                }
            }

            item {
                TransactionQuickActionsRow(
                    onShareReceipt = onShareReceipt,
                    onCopyReference = onCopyReference
                )
            }
        }
    }
}
