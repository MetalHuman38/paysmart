package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel

@Composable
fun ProfileCompletionCard(
    security: LocalSecuritySettingsModel,
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
