package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.navigator.Screen

@Composable
fun StartupScreen(
    navController: NavController,
    onLoginClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    viewModel: LanguageViewModel,
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    StartupContent(
        currentLanguage = currentLang,
        onLanguageClick = {
            navController.navigate(Screen.Language.routeWithOrigin(Screen.Origin.STARTUP))
        },
        onCreateAccountClick = onCreateAccountClick,
        onLoginClick = onLoginClick
    )
}
