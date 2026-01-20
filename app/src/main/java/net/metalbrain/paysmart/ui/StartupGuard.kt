package net.metalbrain.paysmart.ui


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import net.metalbrain.paysmart.domain.state.StartupNavState
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel

@Composable
fun startupGuard(): StartupNavState {
    val viewModel: SecurityViewModel = hiltViewModel()
    var startupState by remember { mutableStateOf<StartupNavState>(StartupNavState.Splash) }

    LaunchedEffect(Unit) {
        delay(2000L)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null || currentUser.uid.isBlank()) {
            startupState = StartupNavState.RequireAuth
            return@LaunchedEffect
        }

        viewModel.fetchSecuritySettings()

        val settings = viewModel.securitySettings.value

        startupState = when {
            settings?.biometricsRequired == true -> StartupNavState.RequireBiometricOptIn
            else -> StartupNavState.App
        }

        startupState = StartupNavState.App
    }

    return startupState
}
