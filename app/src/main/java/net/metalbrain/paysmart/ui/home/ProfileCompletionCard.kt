package net.metalbrain.paysmart.ui.home

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun ProfileCompletionCard(
    user: AuthUserModel,
    onVerifyEmailClick: () -> Unit,
    onAddAddressClick: () -> Unit,
    onVerifyIdentityClick: () -> Unit,
    viewModel: UserViewModel
) {

    ProfileCompletionCardContent(
        user,
        onVerifyEmailClick = onVerifyEmailClick,
        onAddAddressClick = onAddAddressClick,
        onVerifyIdentityClick = onVerifyIdentityClick,
        viewModel = viewModel
    )
}
