package net.metalbrain.paysmart.ui.screens

import androidx.compose.runtime.*
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.viewmodel.AppLoadingViewModel

@Composable
fun SplashScreen(
    viewModel: AppLoadingViewModel = hiltViewModel()
) {
    val message by viewModel.loadingMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startLoading()
    }

    LoadingState(message = message)
}
