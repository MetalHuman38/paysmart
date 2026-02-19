package net.metalbrain.paysmart.ui.transactions.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.Transaction

@Composable
fun TransactionList(transactions: List<Transaction>) {
    // Group by date
    val grouped = transactions.groupBy { it.date }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp)
    ) {
        grouped.forEach { (date, itemsForDate) ->
            item {
                Text(
                    text = date,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(itemsForDate) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}
