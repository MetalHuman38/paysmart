package net.metalbrain.paysmart.ui.home

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.domain.model.SecuritySettings

@Composable
fun ProfileCompletionCard(
    security: SecuritySettings,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {

    ProfileCompletionCardContent(
        security = security,
        onVerifyEmailClick = onVerifyEmailClick,
        onAddAddressClick = onAddAddressClick,
        onVerifyIdentityClick = onVerifyIdentityClick
    )
}
