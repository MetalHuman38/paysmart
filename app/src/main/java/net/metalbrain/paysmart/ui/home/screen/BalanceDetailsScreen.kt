package net.metalbrain.paysmart.ui.home.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceDetailsScreen(
    currencyCode: String,
    amountLabel: String,
    onBack: () -> Unit,
    onSendClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    onConvertClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val normalizedCurrency = currencyCode.trim().uppercase(Locale.US).ifBlank {
        CountryCapabilityCatalog.defaultProfile().currencyCode
    }
    val flag = CurrencyFlagResolver.resolve(context, normalizedCurrency)
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

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
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = stringResource(id = R.string.home_more_actions_content_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = flag, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                text = stringResource(id = R.string.home_balance_currency_title, normalizedCurrency),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                text = stringResource(
                    id = R.string.home_balance_amount_value,
                    amountLabel,
                    normalizedCurrency
                ),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = stringResource(id = R.string.home_view_account_limits),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
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

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

            Text(
                text = stringResource(id = R.string.home_recent_activity_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            BalanceActivityRow(
                title = stringResource(id = R.string.home_balance_activity_transfer_out),
                time = "15:51",
                amount = stringResource(id = R.string.home_balance_amount_negative, "10.00", normalizedCurrency),
                status = stringResource(id = R.string.home_status_successful)
            )
            HorizontalDivider()
            BalanceActivityRow(
                title = stringResource(id = R.string.home_balance_activity_topup),
                time = "15:50",
                amount = stringResource(id = R.string.home_balance_amount_positive, "10.00", normalizedCurrency),
                status = stringResource(id = R.string.home_status_successful)
            )
            HorizontalDivider()
            BalanceActivityRow(
                title = stringResource(id = R.string.home_balance_activity_topup),
                time = "15:49",
                amount = stringResource(id = R.string.home_balance_amount_positive, "10.00", normalizedCurrency),
                status = stringResource(id = R.string.home_status_failed)
            )
        }
    }
}
