package net.metalbrain.paysmart.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.screens.AppLoadingScreen
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun HomeRouter(viewModel: UserViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is UserUiState.Loading -> {
            AppLoadingScreen(modifier = Modifier)
        }
        is UserUiState.ProfileLoaded -> {
            // Optional: Wrap this in SecuredApp
            HomeContent(user = state.user)
        }
        is UserUiState.AuthenticatedButNoProfile -> {
            // Could navigate to a profile setup screen
        }
        is UserUiState.Unauthenticated -> {
            // Shouldn’t happen, but could handle fallback
        }

        else -> {

        }
    }
}
