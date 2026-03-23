package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.data.type.KycDocumentType
import net.metalbrain.paysmart.core.features.identity.provider.formattedLabel
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun IdentityDocumentRow(
    document: KycDocumentType,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected && enabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected && enabled) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = document.leadingIcon(),
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )

            Spacer(modifier = Modifier.width(Dimens.md))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                Text(
                    text = document.formattedLabel,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (!enabled) {
                    Text(
                        text = stringResource(R.string.sheet_not_accepted_inline),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.md))

            Icon(
                imageVector = if (selected) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Outlined.RadioButtonUnchecked
                },
                contentDescription = null,
                tint = when {
                    selected && enabled -> MaterialTheme.colorScheme.primary
                    selected -> MaterialTheme.colorScheme.outline
                    enabled -> MaterialTheme.colorScheme.outline
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.72f)
                }
            )
        }
    }
}

private fun KycDocumentType.leadingIcon(): ImageVector {
    return when (id.lowercase()) {
        "passport" -> Icons.Default.Description
        "drivers_license" -> Icons.Default.CreditCard
        else -> Icons.Default.CreditCard
    }
}
