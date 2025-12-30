package net.metalbrain.paysmart.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.screens.UnauthenticatedScreen
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    navController: NavController,
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
                is UserUiState.ProfileLoaded -> HomeContent(
                    user = state.user,
                    onProfileClick = {
                        navController.navigate(Screen.ProfileScreen.route)
                    },
                    onVerifyEmailClick = {
                        navController.navigate(Screen.AddEmail.route)
                    },
                    onAddAddressClick = {
                        navController.navigate(Screen.AddEmail.route)
                    },
                    onVerifyIdentityClick = {
                        navController.navigate(Screen.AddEmail.route)
                    },

                    viewModel = viewModel
                )
                is UserUiState.Error -> Text("Error: ${state.message}")
            }
        }
    }
}
