package net.metalbrain.paysmart.ui

import net.metalbrain.paysmart.ui.home.HomeScreen
import net.metalbrain.paysmart.SecuredApp
import net.metalbrain.paysmart.ui.language.LanguageSelectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.phone.OTPViewModel
import net.metalbrain.paysmart.ui.auth.OtpVerificationScreen
import net.metalbrain.paysmart.ui.screens.CreateAccountScreen
import net.metalbrain.paysmart.ui.screens.CreateLocalPasswordScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.viewmodel.LoginViewModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import net.metalbrain.paysmart.utils.LocaleUtils
import net.metalbrain.paysmart.utils.formatPhoneNumberForDisplay



sealed class Screen(val route: String) {

    object StartUpGuard : Screen("startup_guard")
    object Startup : Screen("startup")
    object Language : Screen("language")
    object CreateAccount : Screen("create_account")

    object CreatePassword : Screen("create_password")

    object Login : Screen("login")

    object Home : Screen("home")

    object OtpVerification : Screen("otp_verification/{dialCode}/{phoneNumber}") {
        fun routeWithArgs(dialCode: String, phoneNumber: String): String {
            return "otp_verification/${dialCode.trimStart('+')}/${phoneNumber}"
        }
    }
}

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.StartUpGuard.route
    ) {

        composable(Screen.StartUpGuard.route) {
            StartupGuard(navController = navController)
        }


        composable(Screen.Startup.route) {
            val viewModel: LanguageViewModel = hiltViewModel()
            StartupScreen(
                navController = navController,
                onLoginClick = { /* TODO */ },
                onCreateAccountClick = {
                    navController.navigate(Screen.CreateAccount.route)
                },
                viewModel = viewModel,
            )
        }

        composable(Screen.Language.route) {
            val viewModel: LanguageViewModel = hiltViewModel()
            val langCode = viewModel.currentLanguage.collectAsState().value
            val baseContext = LocalContext.current
            val localizedContext = remember(langCode) {
                LocaleUtils.applyLanguage(baseContext, langCode)
            }
            CompositionLocalProvider(LocalContext provides localizedContext) {
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

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("dialCode") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) {
            val otpViewModel: OTPViewModel = hiltViewModel()
            val dialCode = it.arguments?.getString("dialCode") ?: ""
            val rawPhone = it.arguments?.getString("phoneNumber") ?: ""
            val formattedNumber = formatPhoneNumberForDisplay(
                rawNumber = rawPhone,
                dialCode = dialCode
            )
            otpViewModel.setFormattedPhoneNumber(formattedNumber)
            OtpVerificationScreen(
                phoneNumber = formattedNumber,
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                },
                viewModel = otpViewModel,
            )

            otpViewModel.startTimer()
        }

        composable(Screen.CreateAccount.route) {
            val createAccount: CreateAccountViewModel = hiltViewModel()
            val dialCode = createAccount.selectedCountry.value.dialCode
            val rawPhone = createAccount.phoneNumber
            CreateAccountScreen(
                viewModel = createAccount,
                onContinue = {
                    navController.navigate(Screen.OtpVerification.routeWithArgs(dialCode, rawPhone))
                }
                ,
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.CreatePassword.route) {
            val viewModel: CreatePasswordViewModel = hiltViewModel()
            CreateLocalPasswordScreen(
                viewModel = viewModel,
                onDone = {
                    // After setting password, move to Home
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Startup.route) { inclusive = true }
                    }
                }
            )
        }


        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val dialCode = it.arguments?.getString("dialCode") ?: ""
            val rawPhone = it.arguments?.getString("phoneNumber") ?: ""
            val formattedNumber = formatPhoneNumberForDisplay(
                rawNumber = rawPhone,
                dialCode = dialCode
            )


        }

        composable(Screen.Home.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            SecuredApp(viewModel = userViewModel) {
                HomeScreen(viewModel = userViewModel)
            }
        }
    }
}
