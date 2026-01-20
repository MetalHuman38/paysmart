package net.metalbrain.paysmart.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AccountInfoSection() {
    Text(
        text = "💰 My Accounts (Mock)",
        style = MaterialTheme.typography.titleMedium
    )
}



@Composable
fun ExchangeRateCard() {
    Text(
        text = "💱 1 GBP = 1955 NGN (Mock Rate)",
        style = MaterialTheme.typography.bodyLarge
    )
}
