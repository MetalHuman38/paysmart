package net.metalbrain.paysmart.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.home.HomeRouter
import net.metalbrain.paysmart.ui.screens.CreateLocalPasswordScreen
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun StartupGuard(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    var gateState by remember { mutableStateOf(StartupGateState.Splash) }
    val languageViewModel: LanguageViewModel = hiltViewModel()

    val userViewModel: UserViewModel = hiltViewModel()
    val userState by userViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        delay(2000L)

        if (auth.currentUser == null) {
            gateState = StartupGateState.StartupScreen
            return@LaunchedEffect
        }

        // Observe state after auth
        userViewModel.uiState.collectLatest { state ->
            when (state) {
                is UserUiState.ProfileLoaded -> {
                    val profile = state.user
                    gateState = when {
                        !profile.hasLocalPassword -> StartupGateState.PasswordSetup
                        else -> StartupGateState.App
                    }
                }

                is UserUiState.AuthenticatedButNoProfile -> {
                    gateState = StartupGateState.StartupScreen
                }

                is UserUiState.Unauthenticated -> {
                    gateState = StartupGateState.StartupScreen
                }

                else -> Unit
            }
        }
    }

    when (gateState) {
        StartupGateState.Splash -> SplashScreen()

        StartupGateState.StartupScreen -> StartupScreen(
            navController = navController,
            onLoginClick = { navController.navigate(Screen.Login.route) },
            onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
            viewModel = languageViewModel
        )

        StartupGateState.PasswordSetup -> {
            val passwordViewModel: CreatePasswordViewModel = hiltViewModel()
            CreateLocalPasswordScreen(
                viewModel = passwordViewModel,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                }
            )
        }

        StartupGateState.App -> {
            HomeRouter(viewModel = userViewModel)
        }
    }
}

private enum class StartupGateState {
    Splash,
    StartupScreen,
    PasswordSetup,
    App
}
