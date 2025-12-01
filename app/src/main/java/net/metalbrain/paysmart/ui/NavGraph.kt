package net.metalbrain.paysmart.ui

import net.metalbrain.paysmart.ui.language.LanguageSelectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel

sealed class Screen(val route: String) {
    object Startup : Screen("startup")
    object Language : Screen("language")
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: LanguageViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Startup.route
    ) {
        composable(Screen.Startup.route) {
            StartupScreen(
                navController = navController,
                onLoginClick = { /* TODO */ },
                onCreateAccountClick = { /* TODO */ },
                viewModel = viewModel
            )
        }

        composable(Screen.Language.route) {
            val langCode = viewModel.currentLanguage.collectAsState().value
            val selectedLang = supportedLanguages.find { it.code == langCode } ?: supportedLanguages.first()

            LanguageSelectionScreen(
                selectedLanguage = selectedLang,
                onLanguageSelected = { lang ->
                    viewModel.setLanguage(lang.code)
                },
                onContinue = {
                    navController.navigate(Screen.Startup.route)
                }
            )
        }
    }
}
