package net.metalbrain.paysmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.transactions.components.TransactionFilter

@Composable
fun FilterTabs(
    current: TransactionFilter,
    onTabClick: (TransactionFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FilterTab("All", current is TransactionFilter.All) { onTabClick(TransactionFilter.All) }
        FilterTab("Status", current is TransactionFilter.Status) { onTabClick(TransactionFilter.Status) }
        FilterTab("Currency", current is TransactionFilter.Currency) { onTabClick(TransactionFilter.Currency) }
    }
}
