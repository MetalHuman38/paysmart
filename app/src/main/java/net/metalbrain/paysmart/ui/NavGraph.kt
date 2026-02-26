package net.metalbrain.paysmart.ui


import android.net.Uri
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.address.screen.AddressSetupResolverScreen
import net.metalbrain.paysmart.core.features.account.address.viewmodel.AddressSetupResolverViewModel
import net.metalbrain.paysmart.domain.auth.state.LocalSecurityState
import net.metalbrain.paysmart.domain.model.supportedLanguages
import net.metalbrain.paysmart.domain.state.UserUiState
import net.metalbrain.paysmart.ui.home.screen.BalanceDetailsScreen
import net.metalbrain.paysmart.ui.home.screen.HomeScreen
import net.metalbrain.paysmart.ui.home.screen.RewardDetailsScreen
import net.metalbrain.paysmart.core.features.addmoney.screen.AddMoneyScreen
import net.metalbrain.paysmart.core.features.sendmoney.screen.SendMoneyRecipientScreen
import net.metalbrain.paysmart.core.features.language.screen.LanguageSelectionScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureGateScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureRequirement
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionsScreen
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionsViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityProviderHandoffViewModel
import net.metalbrain.paysmart.core.features.account.screen.AccountProtectionContent
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.AddEmailScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricOptInScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricSessionUnlock
import net.metalbrain.paysmart.core.features.account.creation.screen.CreateAccountScreen
import net.metalbrain.paysmart.core.features.account.authorization.password.screen.CreateLocalPasswordScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.EmailSentScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.EmailVerificationSuccessScreen
import net.metalbrain.paysmart.core.features.account.authorization.password.screen.EnterPasswordScreen
import net.metalbrain.paysmart.ui.screens.FederatedLinkingScreen
import net.metalbrain.paysmart.core.features.help.screen.HelpScreen
import net.metalbrain.paysmart.core.features.account.authentication.login.screen.LoginScreen
import net.metalbrain.paysmart.core.features.referral.screen.ReferralScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.SetPasscodeScreen
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.StartupScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.VerifyPasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.core.features.account.authorization.password.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.core.features.account.authorization.password.viewmodel.EnterPasswordViewModel
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel.PasscodeViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.OtpVerificationScreen
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.OTPViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.ReauthOtpScreen
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountInformationScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountStatementScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileDetailsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSubPageScreen
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfileStateViewModel
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePasswordRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePhoneRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.RecoverAccountScreen
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePasswordViewModel
import net.metalbrain.paysmart.core.features.help.viewmodel.HelpViewModel
import net.metalbrain.paysmart.core.features.referral.viewmodel.ReferralViewModel
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import net.metalbrain.paysmart.core.session.SessionViewModel
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import net.metalbrain.paysmart.core.features.identity.screen.IdentityUploadScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityVerifyScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityThirdPartyProviderScreen
import net.metalbrain.paysmart.utils.formatPhoneNumberForDisplay
import java.util.Locale

sealed class Screen(val route: String) {

    object Splash : Screen("splash")

    object Startup : Screen("startup")

    object SecurityGate: Screen("security_gate")


    object BiometricOptIn: Screen("biometric_opt_in")

    object RequireSessionUnlock: Screen("require_session_unlock")


    enum class Origin(val routeValue: String) {
        STARTUP("startup"),
        LOGIN("login"),
        CREATE_ACCOUNT("create_account"),
        PROFILE_ACCOUNT_INFORMATION("profile_account_information");

        companion object {
            fun fromRouteValue(raw: String?): Origin {
                return entries.firstOrNull { it.routeValue == raw } ?: STARTUP
            }
        }
    }

    object Language : Screen("language?origin={origin}") {
        const val BASEROUTE = "language"
        const val ORIGINARG = "origin"
        fun routeWithOrigin(origin: Origin): String {
            return "$BASEROUTE?$ORIGINARG=${Uri.encode(origin.routeValue)}"
        }

        fun routeWithOrigin(origin: String): String {
            return routeWithOrigin(Origin.fromRouteValue(origin))
        }
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

    object Reauthenticate: Screen("reauthenticate?target={target}") {
        const val BASEROUTE = "reauthenticate"
        const val baseRoute = BASEROUTE
        fun routeWithTarget(target: String): String =
            "reauthenticate?target=${Uri.encode(target)}"
    }

    object EnterPassword: Screen("enter_password")


    object ProfileScreen : Screen("profile")

    object ProfileAccountInformation : Screen("profile/account_information")

    object ProfileSecurityPrivacy : Screen("profile/security_privacy")

    object ProfileConnectedAccounts : Screen("profile/connected_accounts")

    object ProfileAbout : Screen("profile/about")

    object ProfileIdentity : Screen("profile/account_information/identity")
    object OnboardingProfile : Screen("onboarding/profile_identity")

    object ProfileAccountLimits : Screen("profile/account_information/account_limits")

    object ProfileAccountStatement : Screen("profile/account_information/account_statement")

    object ProfileAddressResolver : Screen("profile/setup/address_resolver")

    object ProfileIdentityResolver : Screen("profile/setup/identity_resolver")
    object ProfileIdentityResolverVerify : Screen("profile/setup/identity_resolver/local/verify")
    object ProfileIdentityResolverUpload : Screen("profile/setup/identity_resolver/local/upload")
    object ProfileIdentityResolverThirdParty : Screen(
        "profile/setup/identity_resolver/provider?event={event}&sessionId={sessionId}&providerRef={providerRef}"
    ) {
        const val EVENT_ARG = "event"
        const val SESSION_ID_ARG = "sessionId"
        const val PROVIDER_REF_ARG = "providerRef"

        fun routeWithArgs(
            event: String,
            sessionId: String? = null,
            providerRef: String? = null
        ): String {
            return "profile/setup/identity_resolver/provider?$EVENT_ARG=${Uri.encode(event)}&$SESSION_ID_ARG=${Uri.encode(sessionId.orEmpty())}&$PROVIDER_REF_ARG=${Uri.encode(providerRef.orEmpty())}"
        }
    }

    object RecoverAccount : Screen("recover_account?origin={origin}") {
        fun routeWithOrigin(origin: String): String = "recover_account?origin=$origin"
    }
    object ChangePasswordRecovery : Screen("recover_account/change_password")
    object ChangePhoneRecovery : Screen("recover_account/change_phone")


    object Home : Screen("home")
    object AddMoney : Screen("wallet/add_money")
    object SendMoney : Screen("wallet/send_money")

    object FeatureGate : Screen("feature_gate?feature={feature}&resumeRoute={resumeRoute}") {
        const val BASEROUTE = "feature_gate"
        const val FEATUREARG = "feature"
        const val RESUMEROUTEARG = "resumeRoute"

        fun routeWithArgs(feature: String, resumeRoute: String): String {
            return "$BASEROUTE?$FEATUREARG=${Uri.encode(feature)}&$RESUMEROUTEARG=${Uri.encode(resumeRoute)}"
        }
    }

    object BalanceDetails : Screen("wallet_balance/{currency}/{amount}") {
        fun routeWithArgs(currency: String, amount: Double): String {
            val formattedAmount = String.format(Locale.US, "%.2f", amount)
            return "wallet_balance/${Uri.encode(currency)}/${Uri.encode(formattedAmount)}"
        }
    }

    object RewardDetails : Screen("reward_earned/{points}") {
        fun routeWithPoints(points: Double): String {
            val formattedPoints = String.format(Locale.US, "%.2f", points)
            return "reward_earned/${Uri.encode(formattedPoints)}"
        }
    }

    object Transactions: Screen("transactions")

    object Referral: Screen("referral")

    object Help: Screen("help")


    object OtpVerification : Screen("otp_verification/{dialCode}/{phoneNumber}") {
        fun routeWithArgs(dialCode: String, phoneNumber: String): String {
            return "otp_verification/${dialCode.trimStart('+')}/${phoneNumber}"
        }
    }
}

private fun resolveLanguageContinueRoute(origin: Screen.Origin): String {
    return when (origin) {
        Screen.Origin.LOGIN -> Screen.Login.route
        Screen.Origin.CREATE_ACCOUNT -> Screen.CreateAccount.route
        Screen.Origin.PROFILE_ACCOUNT_INFORMATION -> Screen.ProfileAccountInformation.route
        Screen.Origin.STARTUP -> Screen.Startup.route
    }
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
) {
    val activity = LocalActivity.current
    LaunchedEffect(activity?.intent?.dataString) {
        val deepLink = activity?.intent?.data
        val callbackPath = deepLink?.path.orEmpty()
        if (callbackPath.startsWith("/verify/identity/provider")) {
            val event = deepLink?.getQueryParameter("event").orEmpty().ifBlank { "sdk_callback" }
            val sessionId = deepLink?.getQueryParameter("sessionId")
            val providerRef = deepLink?.getQueryParameter("providerRef")
            navController.navigate(
                Screen.ProfileIdentityResolverThirdParty.routeWithArgs(
                    event = event,
                    sessionId = sessionId,
                    providerRef = providerRef
                )
            ) {
                launchSingleTop = true
            }
            return@LaunchedEffect
        }

        val emailLink = deepLink?.toString()?.trim().orEmpty()
        if (emailLink.isNotBlank() && FirebaseAuth.getInstance().isSignInWithEmailLink(emailLink)) {
            navController.navigate(Screen.Login.route) {
                launchSingleTop = true
            }
        }
    }

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
            route = Screen.Language.route,
            arguments = listOf(
                navArgument(Screen.Language.ORIGINARG) {
                    type = NavType.StringType
                    defaultValue = Screen.Origin.STARTUP.routeValue
                }
            )
        ) { backStackEntry ->

            val origin = Screen.Origin.fromRouteValue(
                backStackEntry.arguments?.getString(Screen.Language.ORIGINARG)
            )
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
                    val targetRoute = resolveLanguageContinueRoute(origin)
                    navController.navigate(targetRoute) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = origin == Screen.Origin.PROFILE_ACCOUNT_INFORMATION
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
                onVerificationContinue = {
                    navController.navigate(Screen.OtpVerification.routeWithArgs(dialCode, rawPhone))
                },
                onGetHelpClicked = {
                    navController.navigate(Screen.Help.route) {
                        launchSingleTop = true
                    }
                },
                onSignInClicked = {
                    navController.navigate(Screen.Login.route) {
                        launchSingleTop = true
                    }
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
                    navController.navigate(Screen.RecoverAccount.routeWithOrigin("login"))
                },
                onSignUp = {
                    navController.navigate(Screen.CreateAccount.route)
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Reauthenticate.route,
            arguments = listOf(
                navArgument("target") {
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val viewModel: ReauthOtpViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            val recoveryTarget = backStackEntry.arguments
                ?.getString("target")
                ?.takeIf { it.isNotBlank() }
                ?.let(Uri::decode)

            ReauthOtpScreen(
                viewModel = viewModel,
                activity = activity,
                onSuccess = {
                    if (recoveryTarget != null) {
                        navController.navigate(recoveryTarget) {
                            popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.EnterPassword.route) {
                            popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                        }
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



        composable(
            route = Screen.RecoverAccount.route,
            arguments = listOf(
                navArgument("origin") {
                    defaultValue = "in_app"
                }
            )
        ) { backStackEntry ->
            val origin = backStackEntry.arguments?.getString("origin") ?: "in_app"
            RecoverAccountScreen(
                onBackClick = { navController.popBackStack() },
                onHelpClick = { /* Show Help Dialog or Navigate */ },
                onChangePasswordClick = {
                    if (origin == "login") {
                        navController.navigate(
                            Screen.Reauthenticate.routeWithTarget(
                                Screen.ChangePasswordRecovery.route
                            )
                        )
                    } else {
                        navController.navigate(Screen.ChangePasswordRecovery.route)
                    }
                },
                onChangePhoneClick = {
                    if (origin == "login") {
                        navController.navigate(
                            Screen.Reauthenticate.routeWithTarget(
                                Screen.ChangePhoneRecovery.route
                            )
                        )
                    } else {
                        navController.navigate(Screen.ChangePhoneRecovery.route)
                    }
                }
            )
        }

        composable(Screen.ChangePasswordRecovery.route) {
            val viewModel: ChangePasswordViewModel = hiltViewModel()
            ChangePasswordRecoveryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        composable(Screen.ChangePhoneRecovery.route) {
            val viewModel: ChangePhoneRecoveryViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            ChangePhoneRecoveryScreen(
                viewModel = viewModel,
                activity = activity,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
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

        composable(
            route = "${Screen.AddEmail.route}?returnRoute={returnRoute}",
            arguments = listOf(
                navArgument("returnRoute") {
                    type = NavType.StringType
                    defaultValue = Screen.Home.route
                }
            )
        ) {
            val returnRoute = Uri.decode(it.arguments?.getString("returnRoute") ?: Screen.Home.route)
            AddEmailScreen(
                navController = navController,
                returnRoute = returnRoute
            )
        }

        composable(
            route = "${Screen.EmailSent.route}?returnRoute={returnRoute}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("returnRoute") {
                    type = NavType.StringType
                    defaultValue = Screen.Home.route
                }
            )
        ) {
            val email = Uri.decode(it.arguments?.getString("email") ?: "")
            EmailSentScreen(
                email = email,
                onResend = { /* Handle resend email */ },
                onOpenEmailApp = { /* Handle opening email app */ },
                onChangeEmail = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Screen.EmailVerified.route}?returnRoute={returnRoute}",
            arguments = listOf(
                navArgument("returnRoute") {
                    type = NavType.StringType
                    defaultValue = Screen.Home.route
                }
            )
        ) {
            val returnRoute = Uri.decode(it.arguments?.getString("returnRoute") ?: Screen.Home.route)
            EmailVerificationSuccessScreen(
                onBackToApp = {
                    navController.navigate(returnRoute) {
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Screen.FeatureGate.route,
            arguments = listOf(
                navArgument(Screen.FeatureGate.FEATUREARG) {
                    type = NavType.StringType
                    defaultValue = FeatureKey.ADD_MONEY.id
                },
                navArgument(Screen.FeatureGate.RESUMEROUTEARG) {
                    type = NavType.StringType
                    defaultValue = Screen.Home.route
                }
            )
        ) { backStackEntry ->
            val feature = FeatureKey.fromId(
                backStackEntry.arguments?.getString(Screen.FeatureGate.FEATUREARG)
            )
            val resumeRoute = Uri.decode(
                backStackEntry.arguments?.getString(Screen.FeatureGate.RESUMEROUTEARG)
                    ?: Screen.Home.route
            )
            val gateRoute = Screen.FeatureGate.routeWithArgs(feature.id, resumeRoute)

            val securityViewModel: SecurityViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings
            val decision = FeatureAccessPolicy.evaluate(feature, settings)

            LaunchedEffect(decision.isAllowed, resumeRoute) {
                if (decision.isAllowed) {
                    navController.navigate(resumeRoute) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            FeatureGateScreen(
                feature = feature,
                decision = decision,
                onContinue = {
                    when (decision.nextRequirement) {
                        FeatureRequirement.VERIFIED_EMAIL -> {
                            navController.navigate(
                                "${Screen.AddEmail.route}?returnRoute=${Uri.encode(gateRoute)}"
                            )
                        }

                        FeatureRequirement.HOME_ADDRESS_VERIFIED -> {
                            navController.navigate(Screen.ProfileAddressResolver.route)
                        }

                        FeatureRequirement.IDENTITY_VERIFIED -> {
                            navController.navigate(Screen.ProfileIdentityResolver.route)
                        }

                        null -> {
                            navController.navigate(resumeRoute) {
                                popUpTo(backStackEntry.destination.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AddMoney.route) {
            AddMoneyScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SendMoney.route) {
            SendMoneyRecipientScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
            )
        }

        composable(
            route = Screen.BalanceDetails.route,
            arguments = listOf(
                navArgument("currency") { type = NavType.StringType },
                navArgument("amount") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val currency = Uri.decode(backStackEntry.arguments?.getString("currency") ?: "GBP")
            val amount = Uri.decode(backStackEntry.arguments?.getString("amount") ?: "0.00")
            BalanceDetailsScreen(
                currencyCode = currency,
                amountLabel = amount,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RewardDetails.route,
            arguments = listOf(
                navArgument("points") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val points = Uri.decode(backStackEntry.arguments?.getString("points") ?: "0.00")
            RewardDetailsScreen(
                pointsLabel = points,
                onBack = { navController.popBackStack() }
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
                    onAccountInformationClick = {
                        navController.navigate(Screen.ProfileAccountInformation.route)
                    },
                    onSecurityPrivacyClick = {
                        navController.navigate(Screen.ProfileSecurityPrivacy.route)
                    },
                    onConnectedAccountsClick = {
                        navController.navigate(Screen.ProfileConnectedAccounts.route)
                    },
                    onHelpAndSupportClick = {
                        navController.navigate(Screen.Help.route)
                    },
                    onAboutClick = {
                        navController.navigate(Screen.ProfileAbout.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Startup.route) {
                            popUpTo(0)
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ProfileAccountInformation.route) {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val profileViewModel: ProfileStateViewModel = hiltViewModel()
            val profileState by profileViewModel.uiState.collectAsState()
            val languageCode by languageViewModel.currentLanguage.collectAsState()

            AccountInformationScreen(
                currentLanguage = languageCode,
                profileStatusLabel = when {
                    profileState.isLocked -> stringResource(R.string.profile_status_locked)
                    profileState.isIncomplete -> stringResource(R.string.profile_status_incomplete)
                    else -> stringResource(R.string.profile_status_complete)
                },
                onBack = { navController.popBackStack() },
                onProfileClick = {
                    navController.navigate(Screen.ProfileIdentity.route)
                },
                onAccountLimitsClick = {
                    navController.navigate(Screen.ProfileAccountLimits.route)
                },
                onAccountStatementClick = {
                    navController.navigate(Screen.ProfileAccountStatement.route)
                },
                onLanguageClick = {
                    navController.navigate(
                        Screen.Language.routeWithOrigin(
                            Screen.Origin.PROFILE_ACCOUNT_INFORMATION
                        )
                    )
                }
            )
        }

        composable(Screen.ProfileSecurityPrivacy.route) {
            ProfileSubPageScreen(
                title = stringResource(R.string.profile_menu_security_privacy_title),
                description = stringResource(R.string.profile_security_privacy_description),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileConnectedAccounts.route) {
            ProfileSubPageScreen(
                title = stringResource(R.string.profile_menu_connected_accounts_title),
                description = stringResource(R.string.profile_connected_accounts_description),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileAbout.route) {
            ProfileSubPageScreen(
                title = stringResource(R.string.profile_menu_about_title),
                description = stringResource(R.string.profile_about_description),
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileIdentity.route) {
            val profileViewModel: ProfileStateViewModel = hiltViewModel()
            val profileState by profileViewModel.uiState.collectAsState()
            val profile = profileState.user

            if (profile != null) {
                ProfileDetailsScreen(
                    user = profile,
                    isLocked = profileState.isLocked,
                    missingItems = profileState.missingItems,
                    nextStep = profileState.nextStep,
                    onResolveSetup = {
                        when (profileState.nextStep) {
                            ProfileNextStep.VERIFY_EMAIL -> {
                                navController.navigate(
                                    "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileIdentity.route)}"
                                )
                            }

                            ProfileNextStep.COMPLETE_ADDRESS,
                            ProfileNextStep.VERIFY_IDENTITY -> {
                                navController.navigate(
                                    if (profileState.nextStep == ProfileNextStep.COMPLETE_ADDRESS) {
                                        Screen.ProfileAddressResolver.route
                                    } else {
                                        Screen.ProfileIdentityResolver.route
                                    }
                                )
                            }

                            ProfileNextStep.REVIEW_PROFILE,
                            null -> Unit
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                ProfileSubPageScreen(
                    title = stringResource(R.string.profile_details_title),
                    description = stringResource(R.string.profile_details_loading_message),
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.OnboardingProfile.route) {
            val profileViewModel: ProfileStateViewModel = hiltViewModel()
            val profileState by profileViewModel.uiState.collectAsState()
            val profile = profileState.user

            if (profile != null) {
                ProfileDetailsScreen(
                    user = profile,
                    isLocked = profileState.isLocked,
                    missingItems = profileState.missingItems,
                    nextStep = profileState.nextStep,
                    onResolveSetup = {
                        when (profileState.nextStep) {
                            ProfileNextStep.VERIFY_EMAIL -> {
                                navController.navigate(
                                    "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.OnboardingProfile.route)}"
                                )
                            }

                            ProfileNextStep.COMPLETE_ADDRESS,
                            ProfileNextStep.VERIFY_IDENTITY -> {
                                navController.navigate(
                                    if (profileState.nextStep == ProfileNextStep.COMPLETE_ADDRESS) {
                                        Screen.ProfileAddressResolver.route
                                    } else {
                                        Screen.ProfileIdentityResolver.route
                                    }
                                )
                            }

                            ProfileNextStep.REVIEW_PROFILE,
                            null -> Unit
                        }
                    },
                    showSecuritySetupCta = true,
                    onContinueToSecuritySetup = {
                        navController.navigate(Screen.ProtectAccount.route) {
                            popUpTo(Screen.OnboardingProfile.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                ProfileSubPageScreen(
                    title = stringResource(R.string.profile_details_title),
                    description = stringResource(R.string.profile_details_loading_message),
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ProfileAddressResolver.route) {
            val resolverViewModel: AddressSetupResolverViewModel = hiltViewModel()
            AddressSetupResolverScreen(
                viewModel = resolverViewModel,
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }

        navigation(
            route = Screen.ProfileIdentityResolver.route,
            startDestination = Screen.ProfileIdentityResolverVerify.route
        ) {
            composable(
                route = Screen.ProfileIdentityResolverThirdParty.route,
                arguments = listOf(
                    navArgument(Screen.ProfileIdentityResolverThirdParty.EVENT_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Screen.ProfileIdentityResolverThirdParty.SESSION_ID_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Screen.ProfileIdentityResolverThirdParty.PROVIDER_REF_ARG) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
                }
                val providerViewModel: IdentityProviderHandoffViewModel = hiltViewModel(parentEntry)
                val event = backStackEntry.arguments
                    ?.getString(Screen.ProfileIdentityResolverThirdParty.EVENT_ARG)
                    .orEmpty()
                val sessionId = backStackEntry.arguments
                    ?.getString(Screen.ProfileIdentityResolverThirdParty.SESSION_ID_ARG)
                    ?.takeIf { it.isNotBlank() }
                val providerRef = backStackEntry.arguments
                    ?.getString(Screen.ProfileIdentityResolverThirdParty.PROVIDER_REF_ARG)
                    ?.takeIf { it.isNotBlank() }
                val deepLink = activity?.intent?.data?.toString()

                IdentityThirdPartyProviderScreen(
                    viewModel = providerViewModel,
                    callbackEvent = event,
                    callbackSessionId = sessionId,
                    callbackProviderRef = providerRef,
                    callbackDeepLink = deepLink,
                    onBack = { navController.popBackStack() },
                    onFallbackToLocal = {
                        navController.navigate(Screen.ProfileIdentityResolverVerify.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ProfileIdentityResolverVerify.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
                }
                val resolverViewModel: IdentitySetupResolverViewModel = hiltViewModel(parentEntry)
                IdentityVerifyScreen(
                    viewModel = resolverViewModel,
                    onBack = { navController.popBackStack() },
                    onNext = {
                        navController.navigate(Screen.ProfileIdentityResolverUpload.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.ProfileIdentityResolverUpload.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ProfileIdentityResolver.route)
                }
                val resolverViewModel: IdentitySetupResolverViewModel = hiltViewModel(parentEntry)
                IdentityUploadScreen(
                    viewModel = resolverViewModel,
                    onBackToVerify = { navController.popBackStack() },
                    onDone = {
                        navController.popBackStack(
                            Screen.ProfileIdentityResolver.route,
                            inclusive = true
                        )
                    }
                )
            }
        }

        composable(Screen.ProfileAccountLimits.route) {
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings

            AccountLimitsScreen(
                settings = settings,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileAccountStatement.route) {
            val transactionsViewModel: TransactionsViewModel = hiltViewModel()
            val transactions by transactionsViewModel.filteredTransactions.collectAsState()

            AccountStatementScreen(
                transactions = transactions,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
