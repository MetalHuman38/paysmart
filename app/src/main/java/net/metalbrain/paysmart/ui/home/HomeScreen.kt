package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.screens.EmptyProfileScreen
import net.metalbrain.paysmart.ui.screens.UnauthenticatedScreen
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = { HomeBottomNavigation() }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (val state = uiState) {
                is UserUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(
                    Alignment.Center))
                is UserUiState.Unauthenticated -> UnauthenticatedScreen()
                is UserUiState.AuthenticatedButNoProfile -> EmptyProfileScreen()
                is UserUiState.ProfileLoaded -> HomeContent(user = state.user)
                is UserUiState.Error -> Text("Error: ${state.message}")
            }
        }
    }
}
