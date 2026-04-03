package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun PostOtpCapabilityRow(
    item: CapabilityItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = PaysmartTheme.colorTokens

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfacePrimary,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(Dimens.smallScreenPadding),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        )  {
            // ✅ ICON (fixed position)
            Box(
                modifier = Modifier
                    .size(Dimens.largeSpacing)
                    .background(
                        color = colors.fillHover,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = capabilityIcon(item.key),
                    contentDescription = null,
                    tint = colors.brandPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(Dimens.md))

            // ✅ TEXT BLOCK (aligned properly)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.space8)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                item.footnote?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textTertiary
                    )
                }
            }
        }
    }
}


private fun capabilityIcon(key: CapabilityKey): ImageVector {
    return when (key) {
        CapabilityKey.SEND_INTERNATIONAL -> Icons.Filled.NorthEast
        CapabilityKey.CARD_SPEND_ABROAD -> Icons.Filled.CreditCard
        CapabilityKey.HOLD_AND_CONVERT -> Icons.Filled.CurrencyExchange
        CapabilityKey.RECEIVE_MONEY -> Icons.Filled.AccountBalanceWallet
        CapabilityKey.EARN_RETURN -> Icons.AutoMirrored.Filled.TrendingUp
    }
}
