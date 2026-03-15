package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountStatementScreen(
    transactions: LazyPagingItems<Transaction>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.account_statement_title)) },
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
        val refreshState = transactions.loadState.refresh
        if (refreshState is LoadState.Loading) {
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

        if (refreshState is LoadState.Error && transactions.itemCount == 0) {
            AccountStatementMessageCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                message = refreshState.error.localizedMessage
                    ?: stringResource(R.string.account_statement_empty)
            )
            return@Scaffold
        }

        if (transactions.itemCount == 0) {
            AccountStatementMessageCard(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                message = stringResource(R.string.account_statement_empty)
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                count = transactions.itemCount,
                key = { index -> transactions.peek(index)?.id ?: "transaction_placeholder_$index" }
            ) { index ->
                val transaction = transactions[index] ?: return@items
                AccountStatementTransactionCard(transaction = transaction)
            }

            when (val appendState = transactions.loadState.append) {
                is LoadState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is LoadState.Error -> {
                    item {
                        AccountStatementMessageCard(
                            modifier = Modifier.fillMaxWidth(),
                            message = appendState.error.localizedMessage
                                ?: stringResource(R.string.account_statement_empty)
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}
