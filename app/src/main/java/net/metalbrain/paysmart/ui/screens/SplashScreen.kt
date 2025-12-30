package net.metalbrain.paysmart.ui.screens

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.viewmodel.AppLoadingViewModel

@Composable
fun SplashScreen(
    viewModel: AppLoadingViewModel = hiltViewModel(),
    navController: NavController,
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.loadingMessage.collectAsState()

    // Launch rotating message loop when SplashScreen appears
    LaunchedEffect(Unit) {
        viewModel.startLoading()
    }

    if (isLoading) {
        AppLoadingScreen(message = message)
    } else {
        // Optionally navigate away if not loading
        navController.navigate(Screen.Startup.route)
    }
}
