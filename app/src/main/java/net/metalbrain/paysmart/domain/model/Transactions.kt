package net.metalbrain.paysmart.domain.model

data class Transaction(
    val id: String,
    val title: String,
    val time: String, // "18:58"
    val amount: Double,
    val currency: String, // "GBP", "NGN"
    val status: String, // "Successful", "In Progress", etc.
    val date: String, // "9 Jan 2026" (used for grouping)
    val iconRes: Int // drawable resource for icon (optional)
)
