package net.metalbrain.paysmart.core.features.transactions.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.theme.Dimens

enum class TransactionDetailTab {
    Updates,
    Details
}

@Composable
fun TransactionDetailTabs(
    selectedTab: TransactionDetailTab,
    onTabSelected: (TransactionDetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(Dimens.space4)
        ) {
            TransactionSegmentChip(
                label = stringResource(R.string.transaction_detail_updates_tab),
                selected = selectedTab == TransactionDetailTab.Updates,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(TransactionDetailTab.Updates) }
            )
            TransactionSegmentChip(
                label = stringResource(R.string.transaction_detail_details_tab),
                selected = selectedTab == TransactionDetailTab.Details,
                modifier = Modifier.weight(1f),
                onClick = { onTabSelected(TransactionDetailTab.Details) }
            )
        }
    }
}
