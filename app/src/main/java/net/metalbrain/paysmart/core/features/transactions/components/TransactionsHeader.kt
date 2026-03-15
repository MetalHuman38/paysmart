package net.metalbrain.paysmart.core.features.transactions.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun TransactionsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.space8),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.transactions_title),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
