package net.metalbrain.paysmart.ui


import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import net.metalbrain.paysmart.ui.language.LanguageSelectionScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.phone.OTPViewModel
import net.metalbrain.paysmart.phone.OtpVerificationScreen
import net.metalbrain.paysmart.phone.ReauthOtpScreen
import net.metalbrain.paysmart.phone.ReauthOtpViewModel
import net.metalbrain.paysmart.ui.home.screen.HomeScreen
import net.metalbrain.paysmart.ui.transactions.screen.TransactionsScreen
import net.metalbrain.paysmart.ui.profile.ProfileScreen
import net.metalbrain.paysmart.ui.screens.AccountProtectionContent
import net.metalbrain.paysmart.ui.screens.AddEmailScreen
import net.metalbrain.paysmart.ui.screens.BiometricOptInScreen
import net.metalbrain.paysmart.ui.screens.BiometricSessionUnlock
import net.metalbrain.paysmart.ui.screens.CreateAccountScreen
import net.metalbrain.paysmart.ui.screens.CreateLocalPasswordScreen
import net.metalbrain.paysmart.ui.screens.EmailSentScreen
import net.metalbrain.paysmart.ui.screens.EmailVerificationSuccessScreen
import net.metalbrain.paysmart.ui.screens.EnterPasswordScreen
import net.metalbrain.paysmart.ui.screens.FederatedLinkingScreen
import net.metalbrain.paysmart.ui.help.screen.HelpScreen
import net.metalbrain.paysmart.ui.screens.LoginScreen
import net.metalbrain.paysmart.ui.screens.RecoverAccountScreen
import net.metalbrain.paysmart.ui.referral.screen.ReferralScreen
import net.metalbrain.paysmart.ui.screens.SetPasscodeScreen
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.ui.screens.VerifyPasscodeScreen
import net.metalbrain.paysmart.ui.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.ui.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.ui.viewmodel.EnterPasswordViewModel
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.ui.viewmodel.LoginViewModel
import net.metalbrain.paysmart.ui.viewmodel.PasscodeViewModel
import net.metalbrain.paysmart.ui.help.viewmodel.HelpViewModel
import net.metalbrain.paysmart.ui.referral.viewmodel.ReferralViewModel
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import net.metalbrain.paysmart.core.session.SessionViewModel
import net.metalbrain.paysmart.utils.formatPhoneNumberForDisplay
import androidx.compose.runtime.LaunchedEffect


sealed class Screen(val route: String) {

    object Splash : Screen("splash")

    object Startup : Screen("startup")

    object SecurityGate: Screen("security_gate")


    object BiometricOptIn: Screen("biometric_opt_in")

    object RequireSessionUnlock: Screen("require_session_unlock")


    object Language : Screen("language?origin={origin}") {
        fun routeWithOrigin(origin: String): String = "language?origin=$origin"
    }
    object CreateAccount : Screen("create_account")

    object ProtectAccount: Screen("protect_account")


    object LinkFederatedAccount : Screen("link_federated_account")

    object CreatePassword : Screen("create_password")

    object SetUpPassCode : Screen("set_up_passcode")

    object VerifyPasscode : Screen("verify_passcode")

    object AddEmail : Screen("add_email")

    object EmailSent : Screen("email_sent/{email}") {
        fun routeWithEmail(email: String) = "email_sent/${Uri.encode(email)}"
    }

    object EmailVerified : Screen("email_verified")

    object Login : Screen("login")

    object Reauthenticate: Screen("reauthenticate")

    object EnterPassword: Screen("enter_password")


    object ProfileScreen : Screen("profile")


    object RecoverAccount : Screen("recover_account")


    object Home : Screen("home")

    object Transactions: Screen("transactions")

    object Referral: Screen("referral")

    object Help: Screen("help")


    object OtpVerification : Screen("otp_verification/{dialCode}/{phoneNumber}") {
        fun routeWithArgs(dialCode: String, phoneNumber: String): String {
            return "otp_verification/${dialCode.trimStart('+')}/${phoneNumber}"
        }
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Splash.route) {
            SplashScreen()
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

        composable(Screen.ProtectAccount.route) {
            AccountProtectionContent(
                onSetPasscodeClick = {
                    navController.navigate(Screen.SetUpPassCode.route)
                },
                onSetBiometricClick = {
                    navController.navigate(Screen.BiometricOptIn.route)
                }
            )
        }

        composable(Screen.BiometricOptIn.route) {
            val viewModel: BiometricOptInViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            BiometricOptInScreen(
                activity = activity,
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(Screen.LinkFederatedAccount.route) {
                        popUpTo(Screen.ProtectAccount.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.RequireSessionUnlock.route) {
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val localSettings by securityViewModel.localSecuritySettings.collectAsState()

            val onUnlocked = {
                sessionViewModel.unlockSession {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RequireSessionUnlock.route) { inclusive = true }
                    }
                }
            }

            when {
                localSettings?.biometricsEnabled == true -> {
                    BiometricSessionUnlock(onUnlock = onUnlocked)
                }

                localSettings?.passcodeEnabled == true -> {
                    VerifyPasscodeScreen(onVerified = onUnlocked)
                }

                localSettings?.passwordEnabled == true -> {
                    val enterPasswordViewModel: EnterPasswordViewModel = hiltViewModel()
                    EnterPasswordScreen(
                        viewModel = enterPasswordViewModel,
                        onPasswordCorrect = onUnlocked
                    )
                }

                else -> {
                    LaunchedEffect(Unit) {
                        onUnlocked()
                    }
                }
            }
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
            val langCode by viewModel.currentLanguage.collectAsState()

            val selectedLang =
                supportedLanguages.find { it.code == langCode } ?: supportedLanguages.first()

            LanguageSelectionScreen(
                selectedLanguage = selectedLang,
                onLanguageSelected = { lang ->
                    viewModel.setLanguage(lang.code)
                },
                onContinue = {
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
                },
                onBack = {
                    navController.popBackStack()
                },
                modifier = Modifier
            )
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
                    navController.navigate(Screen.ProtectAccount.route) {
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
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LinkFederatedAccount.route) {
            FederatedLinkingScreen(
                viewModel = hiltViewModel(),
                onSkip = {
                    navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.SecurityGate.route) { inclusive = true }
                    }
                },
                onGoogleLinkSuccess = { navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.LinkFederatedAccount.route) { inclusive = true }
                    }
                },
                onFacebookLinkSuccess = {
                    navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.LinkFederatedAccount.route) { inclusive = true }
                    }
                },
                navController = navController,
            )
        }

        composable(Screen.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val reauthOtpViewModel: ReauthOtpViewModel = hiltViewModel()
            val languageViewModel: LanguageViewModel = hiltViewModel()

            LoginScreen(
                viewModel = loginViewModel,
                reauthOtpViewModel = reauthOtpViewModel,
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
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Reauthenticate.route) {
            val viewModel: ReauthOtpViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity

            ReauthOtpScreen(
                viewModel = viewModel,
                activity = activity,
                onSuccess = {
                    navController.navigate(Screen.EnterPassword.route) {
                        popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.EnterPassword.route) {
            val viewModel: EnterPasswordViewModel = hiltViewModel()
            EnterPasswordScreen(
                viewModel = viewModel,
                onPasswordCorrect = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.EnterPassword.route) { inclusive = true }
                    }
                },
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

        composable(Screen.SetUpPassCode.route) {
            val passCodeviewModel: PasscodeViewModel = hiltViewModel()
            SetPasscodeScreen(
                viewModel = passCodeviewModel,
                onPasscodeSet = {
                    Log.d("SetPasscodeScreen", "onPasscodeSet invoked")
                    navController.navigate(Screen.CreatePassword.route) {
                        popUpTo(Screen.SetUpPassCode.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.VerifyPasscode.route) {
            VerifyPasscodeScreen(
                onVerified = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.VerifyPasscode.route) { inclusive = true }
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
            HomeScreen(
                navController = navController,
            )
        }

        composable(Screen.Transactions.route){
            TransactionsScreen(
                navController = navController
            )
        }

        composable(Screen.Referral.route) {
            val referralViewModel: ReferralViewModel = hiltViewModel()
            ReferralScreen(
                navController = navController,
                viewModel = referralViewModel
            )
        }

        composable(Screen.Help.route) {
            val helpViewModel: HelpViewModel = hiltViewModel()
            HelpScreen(
                navController = navController,
                viewModel = helpViewModel
            )
        }

        composable(Screen.ProfileScreen.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val state by userViewModel.uiState.collectAsState()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val verifiedFromServer =
                (localSecurityState as? LocalSecurityState.Ready)?.settings?.hasVerifiedEmail == true

            if (state is UserUiState.ProfileLoaded) {
                ProfileScreen(
                    user = (state as UserUiState.ProfileLoaded).user,
                    isVerified = verifiedFromServer,
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
