package net.metalbrain.paysmart.core.features.account.creation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey

@Composable
fun PostOtpCapabilityRow(item: CapabilityItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = capabilityIcon(item.key),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = item.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = item.subtitle, style = MaterialTheme.typography.bodyMedium)
        item.footnote?.takeIf { it.isNotBlank() }?.let { footnote ->
            Text(
                text = footnote,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
