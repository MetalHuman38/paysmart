package net.metalbrain.paysmart.core.features.transactions.sheet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionStatusBadge(status: String) {
    val tone = transactionStatusTone(status)
    Surface(
        color = tone.containerColor,
        contentColor = tone.contentColor,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Dimens.sm, vertical = Dimens.xs)
        )
    }
}
