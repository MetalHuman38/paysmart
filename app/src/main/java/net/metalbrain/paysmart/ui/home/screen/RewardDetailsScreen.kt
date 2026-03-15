package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.ui.home.state.RewardDetailsUiState
import net.metalbrain.paysmart.ui.theme.Dimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardDetailsScreen(
    state: RewardDetailsUiState,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.home_rewards_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.common_back)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onHelpClick) {
                        Text(
                            text = stringResource(id = R.string.get_help),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
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
            HomeDetailSectionCard {
                Text(
                    text = String.format(
                        Locale.US,
                        "%.2f%s",
                        state.points,
                        stringResource(id = R.string.home_rewards_points_suffix)
                    ),
                    style = MaterialTheme.typography.headlineMedium
                )
                state.walletUpdatedAtMs?.let { updatedAtMs ->
                    Text(
                        text = stringResource(
                            id = R.string.home_rewards_balance_updated,
                            updatedAtMs.toRewardDateTimeLabel()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HomeDetailSectionCard(tonal = true) {
                HomeDetailEmptyText(
                    text = stringResource(id = R.string.home_rewards_sync_note),
                )
            }

            HomeDetailSectionTitle(
                text = stringResource(id = R.string.home_rewards_recent_transactions_title)
            )

            HomeDetailSectionCard {
                if (state.recentTransactions.isEmpty()) {
                    HomeDetailEmptyText(
                        text = stringResource(id = R.string.home_rewards_no_recent_transactions)
                    )
                } else {
                    state.recentTransactions.forEachIndexed { index, transaction ->
                        RewardTransactionRow(
                            title = transaction.title,
                            subtitle = "${transaction.status} • ${transaction.date}, ${transaction.time}",
                            amount = transaction.toRewardAmountLabel(),
                            onClick = { onTransactionClick(transaction) }
                        )
                        if (index < state.recentTransactions.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

private fun Transaction.toRewardAmountLabel(): String {
    val prefix = if (amount > 0) "+" else ""
    return prefix + String.format(Locale.US, "%.2f %s", amount, currency)
}

private val REWARD_UPDATED_FORMAT: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.US)

private fun Long.toRewardDateTimeLabel(): String {
    return Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(REWARD_UPDATED_FORMAT)
}
