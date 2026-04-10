package net.metalbrain.paysmart.core.features.capabilities.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import net.metalbrain.paysmart.core.features.capabilities.card.AccountLimitCard
import net.metalbrain.paysmart.core.features.capabilities.catalog.AccountLimitKey
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitCardUiState
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitDetailsUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountLimitDetailsContent(
    state: AccountLimitDetailsUiState,
    onTabSelected: (AccountLimitKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        Text(
            text = state.subtitle,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            state.tabs.forEach { tab ->
                AccountLimitTab(
                    title = listOfNotNull(
                        state.flagEmoji.takeIf { it.isNotBlank() },
                        tab.title
                    ).joinToString(" "),
                    selected = state.selectedTab == tab.key,
                    onClick = { onTabSelected(tab.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Dimens.lg)) {
            state.cards.forEach { card ->
                AccountLimitCard(card = card)
            }
        }
    }
}
