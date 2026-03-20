package net.metalbrain.paysmart.core.features.cards.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.cards.data.ManagedCardData
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ManagedCardRow(
    card: ManagedCardData,
    isActionInProgress: Boolean,
    onSetDefault: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.xs,
        shadowElevation = Dimens.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimens.minimumTouchTarget)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = "${card.brand.displayBrand()} •••• ${card.last4}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(
                        R.string.profile_connected_accounts_cards_expiry_format,
                        card.expMonth,
                        card.expYear
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (card.isDefault) {
                    Text(
                        text = stringResource(R.string.profile_connected_accounts_cards_default_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (!card.isDefault) {
                    TextButton(
                        enabled = !isActionInProgress,
                        onClick = onSetDefault
                    ) {
                        Text(text = stringResource(R.string.profile_connected_accounts_cards_make_default_action))
                    }
                }
                TextButton(
                    enabled = !isActionInProgress,
                    onClick = onRemove
                ) {
                    Text(text = stringResource(R.string.profile_connected_accounts_cards_remove_action))
                }
            }
        }
    }
}

private fun String.displayBrand(): String {
    return split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { token ->
            token.replaceFirstChar { char -> char.uppercase() }
        }
        .ifBlank { "Card" }
}
