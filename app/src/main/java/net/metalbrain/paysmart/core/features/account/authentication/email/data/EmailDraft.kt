package net.metalbrain.paysmart.core.features.account.authentication.email.data
import androidx.annotation.Keep

@Keep
data class EmailDraft (
    val email: String? = null,
    val verified: Boolean = false
)
