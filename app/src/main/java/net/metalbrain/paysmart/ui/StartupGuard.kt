package net.metalbrain.paysmart.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.domain.state.StartupNavState
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun StartupGuard(navController: NavController) {
    val userViewModel: UserViewModel = hiltViewModel()
    LocalContext.current
    var startupState by remember { mutableStateOf<StartupNavState>(StartupNavState.Splash) }


    LaunchedEffect(startupState) {
        if (startupState == StartupNavState.App) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.StartUpGuard.route) { inclusive = true }
            }
        }
    }


    LaunchedEffect(Unit) {
        delay(2000L) // splash delay


        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // 🔐 1. Check login
        if (currentUser == null) {
            startupState = StartupNavState.RequireAuth
            return@LaunchedEffect
        }

        val uid = currentUser.uid
        if (uid.isBlank()) {
            startupState = StartupNavState.RequireAuth
            return@LaunchedEffect
        }


        // 🔐 3. Fetch security settings
        val settings = userViewModel.getSecuritySettings()
        if (settings?.passcodeEnabled == true) {
            val shouldLock = userViewModel.shouldLock(settings.lockAfterMinutes)
            if (shouldLock) {
                startupState = StartupNavState.RequirePasscode
                return@LaunchedEffect
            }
        }

        // ✅ 4. All good
        startupState = StartupNavState.App
    }

    when (startupState) {
        StartupNavState.Splash -> SplashScreen(
            navController = navController,
            viewModel = hiltViewModel()
        )
        StartupNavState.RequireAuth -> StartupScreen(
            navController = navController,
            onLoginClick = { navController.navigate(Screen.Login.route) },
            onCreateAccountClick = { navController.navigate(Screen.CreateAccount.route) },
            viewModel = hiltViewModel()
        )
        StartupNavState.RequirePasswordSetup -> {
            navController.navigate(Screen.CreatePassword.route) {
                popUpTo(Screen.StartUpGuard.route) { inclusive = true }
            }
        }
        StartupNavState.RequirePasscode -> {
            // 🔐 2. Check passcode
            navController.navigate(Screen.CreatePassCode.route) {
                popUpTo(Screen.StartUpGuard.route) { inclusive = true }
            }
        }
        StartupNavState.App -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
