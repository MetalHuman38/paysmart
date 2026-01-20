package net.metalbrain.paysmart.email
import androidx.annotation.Keep

@Keep
data class EmailDraft (
    val email: String? = null,
    val verified: Boolean = false
)
