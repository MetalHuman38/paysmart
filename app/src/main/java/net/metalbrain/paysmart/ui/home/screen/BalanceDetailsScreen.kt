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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalanceDetailsScreen(
    currencyCode: String,
    amountLabel: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val normalizedCurrency = currencyCode.trim().uppercase(Locale.US).ifBlank { "GBP" }
    val flag = CurrencyFlagResolver.resolve(context, normalizedCurrency)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = "More")
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
            Text(text = "$normalizedCurrency balance", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(
                text = "$amountLabel $normalizedCurrency",
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
                    Text(text = "View account limits", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                BalanceQuickAction(icon = Icons.Filled.NorthEast, label = "Send")
                BalanceQuickAction(icon = Icons.Filled.Add, label = "Add")
                BalanceQuickAction(icon = Icons.Filled.Remove, label = "Withdraw")
                BalanceQuickAction(icon = Icons.Filled.Autorenew, label = "Convert")
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SegmentChip(label = "Transactions", selected = true, modifier = Modifier.weight(1f))
                    SegmentChip(label = "Account details", selected = false, modifier = Modifier.weight(1f))
                }
            }

            Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            BalanceActivityRow("To Kalejaiye Aderonke Ade...", "15:51", "-10.00 $normalizedCurrency", "Successful")
            HorizontalDivider()
            BalanceActivityRow("Topup via MONZO BANK LIMI...", "15:50", "+10.00 $normalizedCurrency", "Successful")
            HorizontalDivider()
            BalanceActivityRow("Topup via MONZO BANK LIMI...", "15:49", "+10.00 $normalizedCurrency", "Failed")
        }
    }
}
