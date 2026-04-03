package net.metalbrain.paysmart.ui.home.state

data class HomeRecentRecipient(
    val recipientKey: String,
    val displayName: String,
    val subtitle: String,
    val targetCurrencyCode: String = "GBP"
)
