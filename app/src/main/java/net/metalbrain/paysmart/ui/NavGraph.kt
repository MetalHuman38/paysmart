package net.metalbrain.paysmart.ui


import android.net.Uri
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
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
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
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
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionsScreen
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionsViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityProviderHandoffViewModel
import net.metalbrain.paysmart.core.features.account.screen.AccountProtectionContent
import net.metalbrain.paysmart.core.features.account.passkey.screen.PasskeySetupScreen
import net.metalbrain.paysmart.core.features.account.passkey.screen.ProfilePasskeySettingsScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.AddEmailScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricOptInScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.screen.BiometricSessionUnlock
import net.metalbrain.paysmart.core.features.account.creation.screen.CreateAccountScreen
import net.metalbrain.paysmart.core.features.account.creation.screen.ClientInformationScreen
import net.metalbrain.paysmart.core.features.account.creation.screen.PostOtpSecurityStepsScreen
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
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.core.features.language.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.core.features.account.authentication.login.viewmodel.LoginViewModel
import net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel.PasscodeViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.OtpVerificationScreen
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.OTPViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.viewModel.ReauthOtpViewModel
import net.metalbrain.paysmart.core.features.account.creation.phone.screen.ReauthOtpScreen
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileScreen
import net.metalbrain.paysmart.core.features.account.creation.screen.PostOtpCapabilitiesScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountInformationScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountStatementScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileDetailsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSecurityPrivacyScreen
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
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaNudgeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.PostOtpCapabilitiesViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.EmailSentViewModel
import net.metalbrain.paysmart.core.features.theme.viewmodel.AppThemeViewModel
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
    }
    
    object CreateAccount : Screen("create_account")

    object ProtectAccount: Screen("protect_account")

    object PasskeySetup : Screen("passkey_setup")


    object LinkFederatedAccount : Screen("link_federated_account") {
        const val RETURN_ROUTE_ARG = "returnRoute"
        fun routeWithReturn(returnRoute: String): String {
            return "${route}?$RETURN_ROUTE_ARG=${Uri.encode(returnRoute)}"
        }
    }

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
    object ProfilePasskeySettings : Screen("profile/security_privacy/passkey")

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


    object OtpVerification :
        Screen("otp_verification/{dialCode}/{phoneNumber}?countryIso2={countryIso2}") {
        const val COUNTRY_ISO2_ARG = "countryIso2"

        fun routeWithArgs(
            dialCode: String,
            phoneNumber: String,
            countryIso2: String = DEFAULT_COUNTRY_ISO2
        ): String {
            val normalizedIso2 = normalizeCountryIso2(countryIso2)
            return "otp_verification/${dialCode.trimStart('+')}/${phoneNumber}?$COUNTRY_ISO2_ARG=${Uri.encode(normalizedIso2)}"
        }
    }

    object PostOtpCapabilities : Screen("onboarding/capabilities/{countryIso2}") {
        fun routeWithCountry(countryIso2: String): String {
            return "onboarding/capabilities/${Uri.encode(normalizeCountryIso2(countryIso2))}"
        }
    }

    object PostOtpClientInformation : Screen("onboarding/client_information/{countryIso2}") {
        fun routeWithCountry(countryIso2: String): String {
            return "onboarding/client_information/${Uri.encode(normalizeCountryIso2(countryIso2))}"
        }
    }

    object PostOtpSecuritySteps : Screen("onboarding/security_steps/{countryIso2}") {
        fun routeWithCountry(countryIso2: String): String {
            return "onboarding/security_steps/${Uri.encode(normalizeCountryIso2(countryIso2))}"
        }
    }

    object PostOtpMfaNudge : Screen("onboarding/mfa_nudge/{countryIso2}") {
        fun routeWithCountry(countryIso2: String): String {
            return "onboarding/mfa_nudge/${Uri.encode(normalizeCountryIso2(countryIso2))}"
        }
    }

    object ProfileMfaNudge : Screen("profile/security/mfa_nudge")
}

private fun resolveLanguageContinueRoute(origin: Screen.Origin): String {
    return when (origin) {
        Screen.Origin.LOGIN -> Screen.Login.route
        Screen.Origin.CREATE_ACCOUNT -> Screen.CreateAccount.route
        Screen.Origin.PROFILE_ACCOUNT_INFORMATION -> Screen.ProfileAccountInformation.route
        Screen.Origin.STARTUP -> Screen.Startup.route
    }
}

private fun openEmailApp(context: Context): Boolean {
    val emailAppIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_APP_EMAIL)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(emailAppIntent)
        return true
    } catch (_: ActivityNotFoundException) {
        val fallback = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(fallback)
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
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
                },
                onSetPasskeyClick = {
                    navController.navigate(Screen.PasskeySetup.route)
                }
            )
        }

        composable(Screen.PasskeySetup.route) {
            val viewModel: PasskeySetupViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            PasskeySetupScreen(
                activity = activity,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BiometricOptIn.route) {
            val viewModel: BiometricOptInViewModel = hiltViewModel()
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
            val hasReadyPassword = localSettings?.passwordEnabled == true &&
                localSettings.localPasswordSetAt != null
            val activity = LocalActivity.current as FragmentActivity
            BiometricOptInScreen(
                activity = activity,
                viewModel = viewModel,
                onSuccess = {
                    val targetRoute = if (hasReadyPassword) {
                        Screen.Home.route
                    } else {
                        Screen.CreatePassword.route
                    }
                    navController.navigate(targetRoute) {
                        popUpTo(Screen.ProtectAccount.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(Screen.RequireSessionUnlock.route) {
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val sessionViewModel: SessionViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings

            val onUnlocked = {
                sessionViewModel.unlockSession {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RequireSessionUnlock.route) { inclusive = true }
                    }
                }
            }
            val onLogout = {
                userViewModel.signOut()
                navController.navigate(Screen.Startup.route) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }

            when {
                localSecurityState is LocalSecurityState.Loading -> {
                    SplashScreen()
                }

                localSettings?.biometricsEnabled == true -> {
                    BiometricSessionUnlock(
                        onUnlock = onUnlocked,
                        onLogout = onLogout
                    )
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
                    AccountProtectionContent(
                        onSetPasscodeClick = {
                            navController.navigate(Screen.SetUpPassCode.route) {
                                launchSingleTop = true
                            }
                        },
                        onSetBiometricClick = {
                            navController.navigate(Screen.BiometricOptIn.route) {
                                launchSingleTop = true
                            }
                        },
                        onSetPasskeyClick = {
                            navController.navigate(Screen.PasskeySetup.route) {
                                launchSingleTop = true
                            }
                        }
                    )
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
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument(Screen.OtpVerification.COUNTRY_ISO2_ARG) {
                    type = NavType.StringType
                    defaultValue = DEFAULT_COUNTRY_ISO2
                }
            )
        ) {
            val otpViewModel: OTPViewModel = hiltViewModel()
            val dialCode = it.arguments?.getString("dialCode") ?: ""
            val rawPhone = it.arguments?.getString("phoneNumber") ?: ""
            val countryIso2 = normalizeCountryIso2(
                it.arguments?.getString(Screen.OtpVerification.COUNTRY_ISO2_ARG)
            )
            val formattedNumber = formatPhoneNumberForDisplay(
                rawNumber = rawPhone,
                dialCode = dialCode
            )
            otpViewModel.setFormattedPhoneNumber(formattedNumber)
            OtpVerificationScreen(
                phoneNumber = formattedNumber,
                onContinue = {
                    navController.navigate(Screen.PostOtpCapabilities.routeWithCountry(countryIso2)) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = otpViewModel,
            )

            otpViewModel.startTimer()
        }

        composable(
            route = Screen.PostOtpCapabilities.route,
            arguments = listOf(
                navArgument("countryIso2") {
                    type = NavType.StringType
                    defaultValue = DEFAULT_COUNTRY_ISO2
                }
            )
        ) { backStackEntry ->
            val capabilitiesViewModel: PostOtpCapabilitiesViewModel = hiltViewModel()
            val countryIso2 = normalizeCountryIso2(
                backStackEntry.arguments?.getString("countryIso2")
            )
            val currentDestinationId = backStackEntry.destination.id

            PostOtpCapabilitiesScreen(
                countryIso2 = countryIso2,
                viewModel = capabilitiesViewModel,
                onBack = { navController.popBackStack() },
                onNext = {
                    navController.navigate(Screen.PostOtpSecuritySteps.routeWithCountry(countryIso2)) {
                        popUpTo(currentDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PostOtpSecuritySteps.route,
            arguments = listOf(
                navArgument("countryIso2") {
                    type = NavType.StringType
                    defaultValue = DEFAULT_COUNTRY_ISO2
                }
            )
        ) { backStackEntry ->
            val countryIso2 = normalizeCountryIso2(
                backStackEntry.arguments?.getString("countryIso2")
            )
            val currentDestinationId = backStackEntry.destination.id

            PostOtpSecurityStepsScreen(
                countryIso2 = countryIso2,
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.PostOtpClientInformation.routeWithCountry(countryIso2)) {
                        popUpTo(currentDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PostOtpClientInformation.route,
            arguments = listOf(
                navArgument("countryIso2") {
                    type = NavType.StringType
                    defaultValue = DEFAULT_COUNTRY_ISO2
                }
            )
        ) { backStackEntry ->
            val countryIso2 = normalizeCountryIso2(
                backStackEntry.arguments?.getString("countryIso2")
            )
            val clientInfoViewModel: ClientInformationViewModel = hiltViewModel()
            val currentDestinationId = backStackEntry.destination.id

            ClientInformationScreen(
                countryIso2 = countryIso2,
                viewModel = clientInfoViewModel,
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigate(Screen.PostOtpMfaNudge.routeWithCountry(countryIso2)) {
                        popUpTo(currentDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PostOtpMfaNudge.route,
            arguments = listOf(
                navArgument("countryIso2") {
                    type = NavType.StringType
                    defaultValue = DEFAULT_COUNTRY_ISO2
                }
            )
        ) { backStackEntry ->
            val countryIso2 = normalizeCountryIso2(
                backStackEntry.arguments?.getString("countryIso2")
            )
            val mfaViewModel: MfaNudgeViewModel = hiltViewModel()
            val currentDestinationId = backStackEntry.destination.id

            MfaNudgeScreen(
                viewModel = mfaViewModel,
                onBack = { navController.popBackStack() },
                onPrimaryAction = { hasVerifiedEmail ->
                    if (hasVerifiedEmail) {
                        navController.navigate(Screen.ProtectAccount.route) {
                            popUpTo(currentDestinationId) { inclusive = true }
                        }
                    } else {
                        navController.navigate(
                            Screen.LinkFederatedAccount.routeWithReturn(Screen.ProtectAccount.route)
                        )
                    }
                },
                onSkip = {
                    navController.navigate(Screen.ProtectAccount.route) {
                        popUpTo(currentDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateAccount.route) {
            val createAccount: CreateAccountViewModel = hiltViewModel()
            val dialCode = createAccount.selectedCountry.value.dialCode
            val rawPhone = createAccount.phoneNumber
            CreateAccountScreen(
                viewModel = createAccount,
                onVerificationContinue = { countryIso2 ->
                    navController.navigate(
                        Screen.OtpVerification.routeWithArgs(
                            dialCode = dialCode,
                            phoneNumber = rawPhone,
                            countryIso2 = countryIso2
                        )
                    )
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

        composable(
            route = "${Screen.LinkFederatedAccount.route}?${Screen.LinkFederatedAccount.RETURN_ROUTE_ARG}={${Screen.LinkFederatedAccount.RETURN_ROUTE_ARG}}",
            arguments = listOf(
                navArgument(Screen.LinkFederatedAccount.RETURN_ROUTE_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val returnRoute = Uri.decode(
                backStackEntry.arguments
                    ?.getString(Screen.LinkFederatedAccount.RETURN_ROUTE_ARG)
                    .orEmpty()
            )
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val localSettings = (localSecurityState as? LocalSecurityState.Ready)?.settings
            val hasReadyPassword = localSettings?.passwordEnabled == true &&
                localSettings.localPasswordSetAt != null
            val nextRouteAfterLinkNudge = if (hasReadyPassword) {
                Screen.Home.route
            } else {
                Screen.CreatePassword.route
            }
            val destination = returnRoute.ifBlank { nextRouteAfterLinkNudge }

            FederatedLinkingScreen(
                viewModel = hiltViewModel(),
                emailReturnRoute = destination,
                onSkip = {
                    navController.navigate(destination) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoogleLinkSuccess = { navController.navigate(destination) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onFacebookLinkSuccess = {
                    navController.navigate(destination) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
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
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.SetUpPassCode.route) {
            val passCodeviewModel: PasscodeViewModel = hiltViewModel()
            SetPasscodeScreen(
                viewModel = passCodeviewModel,
                onPasscodeSet = {
                    Log.d("SetPasscodeScreen", "onPasscodeSet invoked")
                    navController.navigate(Screen.Home.route) {
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
            val returnRoute = Uri.decode(it.arguments?.getString("returnRoute") ?: Screen.Home.route)
            val context = LocalContext.current
            val emailSentViewModel: EmailSentViewModel = hiltViewModel()
            val emailSentState by emailSentViewModel.uiState.collectAsState()

            LaunchedEffect(emailSentState.infoMessage, emailSentState.errorMessage) {
                val message = emailSentState.infoMessage ?: emailSentState.errorMessage
                if (!message.isNullOrBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    emailSentViewModel.consumeTransientMessage()
                }
            }

            EmailSentScreen(
                email = email,
                onResend = {
                    emailSentViewModel.resendVerificationEmail(email)
                },
                onOpenEmailApp = {
                    if (!openEmailApp(context)) {
                        Toast.makeText(context, "No email app found.", Toast.LENGTH_SHORT).show()
                    }
                },
                onChangeEmail = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                isResending = emailSentState.isResending,
                infoMessage = emailSentState.infoMessage,
                errorMessage = emailSentState.errorMessage
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
            val currency = Uri.decode(
                backStackEntry.arguments?.getString("currency")
                    ?: CountryCapabilityCatalog.defaultProfile().currencyCode
            )
            val amount = Uri.decode(backStackEntry.arguments?.getString("amount") ?: "0.00")
            BalanceDetailsScreen(
                currencyCode = currency,
                amountLabel = amount,
                onBack = { navController.popBackStack() },
                onSendClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                },
                onAddClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.ADD_MONEY.id,
                            resumeRoute = Screen.AddMoney.route
                        )
                    )
                },
                onWithdrawClick = {
                    navController.navigate(Screen.Help.route) {
                        launchSingleTop = true
                    }
                },
                onConvertClick = {
                    navController.navigate(Screen.SendMoney.route) {
                        launchSingleTop = true
                    }
                }
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
            val appThemeViewModel: AppThemeViewModel = hiltViewModel()
            val profileState by profileViewModel.uiState.collectAsState()
            val languageCode by languageViewModel.currentLanguage.collectAsState()
            val themeMode by appThemeViewModel.themeMode.collectAsState()

            AccountInformationScreen(
                currentLanguage = languageCode,
                currentThemeMode = themeMode,
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
                },
                onThemeModeClick = {
                    appThemeViewModel.cycleThemeMode()
                }
            )
        }

        composable(Screen.ProfileSecurityPrivacy.route) {
            val securityViewModel: SecurityViewModel = hiltViewModel()
            val localSecurityState by securityViewModel.localSecurityState.collectAsState()
            val hideBalanceEnabled by securityViewModel.hideBalanceEnabled.collectAsState()
            val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings

            ProfileSecurityPrivacyScreen(
                settings = settings,
                hideBalanceEnabled = hideBalanceEnabled,
                onBack = { navController.popBackStack() },
                onResetPassword = {
                    navController.navigate(Screen.ChangePasswordRecovery.route) {
                        launchSingleTop = true
                    }
                },
                onTransactionPin = {
                    navController.navigate(Screen.SetUpPassCode.route) {
                        launchSingleTop = true
                    }
                },
                onPasskeySettings = {
                    navController.navigate(Screen.ProfilePasskeySettings.route) {
                        launchSingleTop = true
                    }
                },
                onBiometricToggle = { enabled ->
                    if (enabled) {
                        navController.navigate(Screen.BiometricOptIn.route) {
                            launchSingleTop = true
                        }
                    } else {
                        securityViewModel.clearBiometricOptIn()
                    }
                },
                onViewPrivacySettings = {
                    navController.navigate(Screen.ProfileAbout.route) {
                        launchSingleTop = true
                    }
                },
                onHideBalanceToggle = securityViewModel::setHideBalance
            )
        }

        composable(Screen.ProfilePasskeySettings.route) {
            val viewModel: PasskeySetupViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            ProfilePasskeySettingsScreen(
                activity = activity,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileMfaNudge.route) { backStackEntry ->
            val mfaViewModel: MfaNudgeViewModel = hiltViewModel()
            val currentDestinationId = backStackEntry.destination.id

            MfaNudgeScreen(
                viewModel = mfaViewModel,
                onBack = { navController.popBackStack() },
                onPrimaryAction = { hasVerifiedEmail ->
                    if (hasVerifiedEmail) {
                        navController.navigate(Screen.ProfileSecurityPrivacy.route) {
                            popUpTo(currentDestinationId) { inclusive = true }
                        }
                    } else {
                        navController.navigate(
                            "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileMfaNudge.route)}"
                        )
                    }
                },
                onSkip = { navController.popBackStack() }
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
                    showMfaNudgeCta = profileState.security?.hasSkippedMfaEnrollmentPrompt == true,
                    onOpenMfaNudge = {
                        navController.navigate(Screen.ProfileMfaNudge.route) {
                            launchSingleTop = true
                        }
                    },
                    showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                    onOpenPasskeyNudge = {
                        navController.navigate(Screen.ProfilePasskeySettings.route) {
                            launchSingleTop = true
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
                                    Screen.LinkFederatedAccount.routeWithReturn(Screen.OnboardingProfile.route)
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
                    showMfaNudgeCta = profileState.security?.hasSkippedMfaEnrollmentPrompt == true,
                    onOpenMfaNudge = {
                        navController.navigate(Screen.ProfileMfaNudge.route) {
                            launchSingleTop = true
                        }
                    },
                    showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                    onOpenPasskeyNudge = {
                        navController.navigate(Screen.ProfilePasskeySettings.route) {
                            launchSingleTop = true
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
