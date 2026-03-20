package net.metalbrain.paysmart.core.features.help.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.transactions.components.TransactionItem
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpRecentActivityScreen(
    topic: HelpSupportTopic,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onContinueWithoutSelection: () -> Unit
) {
    val listTitleRes = requireNotNull(topic.listTitleRes)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(listTitleRes)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Dimens.screenPadding,
                top = Dimens.lg,
                end = Dimens.screenPadding,
                bottom = Dimens.lg
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            if (transactions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        tonalElevation = Dimens.xs
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md)
                        ) {
                            Text(
                                text = stringResource(R.string.help_recent_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.help_recent_empty_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(onClick = onContinueWithoutSelection) {
                                Text(text = stringResource(R.string.help_recent_continue_action))
                            }
                        }
                    }
                }
            } else {
                items(transactions, key = { transaction -> transaction.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}
