package net.metalbrain.paysmart.ui

import android.net.Uri
import net.metalbrain.paysmart.ui.home.HomeScreen
import net.metalbrain.paysmart.SecuredApp
import net.metalbrain.paysmart.ui.language.LanguageSelectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.phone.OTPViewModel
import net.metalbrain.paysmart.phone.OtpVerificationScreen
import net.metalbrain.paysmart.ui.profile.ProfileScreen
import net.metalbrain.paysmart.ui.screens.AddEmailScreen
import net.metalbrain.paysmart.ui.screens.CreateAccountScreen
import net.metalbrain.paysmart.ui.screens.CreateLocalPasswordScreen
import net.metalbrain.paysmart.ui.screens.EmailSentScreen
import net.metalbrain.paysmart.ui.screens.EmailVerificationSuccessScreen
import net.metalbrain.paysmart.ui.screens.LoginScreen
import net.metalbrain.paysmart.ui.screens.RecoverAccountScreen
import net.metalbrain.paysmart.ui.screens.SetPasscodeScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.screens.VerifyPasscodeScreen
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
    object Language : Screen("language?origin={origin}") {
        fun routeWithOrigin(origin: String): String = "language?origin=$origin"
    }
    object CreateAccount : Screen("create_account")

    object CreatePassword : Screen("create_password")

    object CreatePassCode : Screen("create_passcode")

    object AddEmail : Screen("add_email")

    object EmailSent : Screen("email_sent/{email}") {
        fun routeWithEmail(email: String) = "email_sent/${Uri.encode(email)}"
    }

    object EmailVerified : Screen("email_verified")


    object Login : Screen("login")

    object ProfileScreen : Screen("profile")


    object RecoverAccount : Screen("recover_account")


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
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onCreateAccountClick = {
                    navController.navigate(Screen.CreateAccount.route)
                },
                viewModel = viewModel,
            )
        }

        composable(
            route = "language?origin={origin}",
            arguments = listOf(
                navArgument("origin") {
                    defaultValue = "startup"
                }
            )
        ) { backStackEntry ->

            val origin = backStackEntry.arguments?.getString("origin") ?: "startup"
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
                        // ✅ Navigate back to previous screen, not always startup
                        when (origin) {
                            "login" -> navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Language.route) { inclusive = true }
                            }
                            "create_account" -> navController.navigate(Screen.CreateAccount.route) {
                                popUpTo(Screen.Language.route) { inclusive = true }
                            }
                            else -> navController.navigate(Screen.Startup.route) {
                                popUpTo(Screen.Language.route) { inclusive = true }
                            }
                        }
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
                onContinue = {
                    navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
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

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val languageViewModel: LanguageViewModel = hiltViewModel()

            LoginScreen(
                viewModel = loginViewModel,
                languageViewModel = languageViewModel,
                navController = navController,
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(Screen.RecoverAccount.route)
                },
                onSignUp = {
                    navController.navigate(Screen.CreateAccount.route)
                }
                ,
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.RecoverAccount.route) {
            RecoverAccountScreen(
                onBackClick = { navController.popBackStack() },
                onHelpClick = { /* Show Help Dialog or Navigate */ },
                onChangePasswordClick = { /* Change password */ },
                onChangePhoneClick = { /* Change phone number */ }
            )
        }


        composable(Screen.CreatePassword.route) {
            val viewModel: CreatePasswordViewModel = hiltViewModel()
            CreateLocalPasswordScreen(
                viewModel = viewModel,
                onDone = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(Screen.CreatePassCode.route) {
            SetPasscodeScreen(
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.CreatePassCode.route) { inclusive = true }
                    }
                }
            )
        }

        composable("verify_passcode") {
            VerifyPasscodeScreen(
                onVerified = {
                    navController.navigate("home") {
                        popUpTo("verify_passcode") { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.AddEmail.route) {
            AddEmailScreen(
                navController = navController

            )
        }

        composable(Screen.EmailSent.route) {
            val email = it.arguments?.getString("email") ?: ""
            EmailSentScreen(
                email = email,
                onResend = { /* Handle resend email */ },
                onOpenEmailApp = { /* Handle opening email app */ },
                onChangeEmail = { /* Handle changing email */ },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.EmailVerified.route) {
            EmailVerificationSuccessScreen(
                onBackToApp = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0)
                    }
                }
            )
        }



        composable(Screen.Home.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            SecuredApp(viewModel = userViewModel) {
                HomeScreen(
                    navController = navController,
                    viewModel = userViewModel
                )
            }
        }

        composable(Screen.ProfileScreen.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            val state by userViewModel.uiState.collectAsState()

            if (state is UserUiState.ProfileLoaded) {
                ProfileScreen(
                    user = (state as UserUiState.ProfileLoaded).user,
                    viewModel = userViewModel,
                    onLogout = {
                        navController.navigate(Screen.Startup.route) {
                            popUpTo(0)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
