package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityItem
import net.metalbrain.paysmart.core.features.capabilities.catalog.CapabilityKey

@Composable
fun ServiceItemCard(service: CapabilityItem) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .width(92.dp)
                .padding(2.dp)
        ) {
            Box(
                modifier = Modifier.padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon(),
                    contentDescription = service.title,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Text(
            text = service.title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun CapabilityItem.icon() = when (key) {
    CapabilityKey.SEND_INTERNATIONAL -> Icons.Filled.Public
    CapabilityKey.CARD_SPEND_ABROAD -> Icons.Filled.CreditCard
    CapabilityKey.HOLD_AND_CONVERT -> Icons.Filled.SwapHoriz
    CapabilityKey.RECEIVE_MONEY -> Icons.Filled.AccountBalance
    CapabilityKey.EARN_RETURN -> Icons.AutoMirrored.Filled.TrendingUp
}
