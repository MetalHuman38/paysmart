package net.metalbrain.paysmart.ui.home

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.domain.model.AuthUserModel

@Composable
fun ProfileCompletionCard(
    user: AuthUserModel,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit
) {
    val profileState = ProfileSetupState(
        hasVerifiedEmail = user.hasVerifiedEmail,
        hasAddedHomeAddress = user.hasAddedHomeAddress,
        hasVerifiedIdentity = user.hasVerifiedIdentity
    )

    ProfileCompletionCardContent(
        profileState = profileState,
        onAddAddressClick = onAddAddressClick,
        onVerifyIdentityClick = onVerifyIdentityClick
    )
}
