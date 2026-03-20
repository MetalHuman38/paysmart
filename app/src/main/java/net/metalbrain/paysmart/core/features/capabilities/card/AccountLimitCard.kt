package net.metalbrain.paysmart.core.features.capabilities.card

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
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitCardUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AccountLimitCard(card: AccountLimitCardUiState) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.sm,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleLarge
            )

            LinearProgressIndicator(
                progress = { card.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.sm),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = card.leadingLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = card.trailingLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
