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
import androidx.navigation.NavOptionsBuilder
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
import net.metalbrain.paysmart.ui.home.screen.BalanceDetailsRoute
import net.metalbrain.paysmart.ui.home.screen.HomeScreen
import net.metalbrain.paysmart.ui.home.screen.RewardDetailsRoute
import net.metalbrain.paysmart.core.features.addmoney.screen.AddMoneyScreen
import net.metalbrain.paysmart.core.features.sendmoney.screen.SendMoneyRecipientScreen
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceDetailRoute
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceVenueSetupRoute
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceWorkerProfileRoute
import net.metalbrain.paysmart.core.features.language.screen.LanguageSelectionScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureAccessPolicy
import net.metalbrain.paysmart.core.features.featuregate.FeatureGateScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.featuregate.FeatureRequirement
import net.metalbrain.paysmart.core.features.fx.screen.ExchangeRatesScreen
import net.metalbrain.paysmart.core.features.fx.viewmodel.ExchangeRatesViewModel
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionDetailRoute
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionsScreen
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionDetailViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityProviderHandoffViewModel
import net.metalbrain.paysmart.core.features.account.screen.AccountProtectionContent
import net.metalbrain.paysmart.core.features.account.passkey.screen.PasskeySetupScreen
import net.metalbrain.paysmart.core.features.account.passkey.screen.ProfilePasskeySettingsScreen
import net.metalbrain.paysmart.core.features.account.authentication.email.screen.AddEmailScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.provider.BiometricHelper
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
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.ChangePasscodeBiometricGateScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.ChangePasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.SetPasscodeScreen
import net.metalbrain.paysmart.ui.screens.SplashScreen
import net.metalbrain.paysmart.ui.screens.startup.StartupScreen
import net.metalbrain.paysmart.core.features.account.authorization.passcode.screen.VerifyPasscodeScreen
import net.metalbrain.paysmart.core.features.account.authorization.biometric.viewmodel.BiometricOptInViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.CreateAccountViewModel
import net.metalbrain.paysmart.ui.home.viewmodel.BalanceDetailsViewModel
import net.metalbrain.paysmart.ui.home.viewmodel.RewardDetailsViewModel
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
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitDetailsRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountLimitsRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.AccountStatementRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileAboutScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileAboutSocialsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfilePhotoPickerRoute
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileDetailsScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSecurityPrivacyScreen
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileSubPageScreen
import net.metalbrain.paysmart.core.features.account.profile.state.ProfileNextStep
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.AccountStatementViewModel
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfilePhotoViewModel
import net.metalbrain.paysmart.core.features.account.profile.viewmodel.ProfileStateViewModel
import net.metalbrain.paysmart.core.features.account.profile.screen.ProfileConnectedAccountsRoute
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePasswordRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.ChangePhoneRecoveryScreen
import net.metalbrain.paysmart.core.features.account.recovery.screen.RecoverAccountScreen
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePasswordViewModel
import net.metalbrain.paysmart.core.features.help.viewmodel.HelpViewModel
import net.metalbrain.paysmart.core.features.referral.viewmodel.ReferralViewModel
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaNudgeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.screen.MfaSignInChallengeScreen
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaNudgeViewModel
import net.metalbrain.paysmart.core.features.account.security.mfa.viewmodel.MfaSignInViewModel
import net.metalbrain.paysmart.core.features.cards.viewmodel.ManagedCardsViewModel
import net.metalbrain.paysmart.core.features.fundingaccount.screen.FundingAccountRoute
import net.metalbrain.paysmart.core.features.fundingaccount.viewmodel.FundingAccountViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.PostOtpCapabilitiesViewModel
import net.metalbrain.paysmart.core.features.account.creation.viewmodel.ClientInformationViewModel
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.EmailSentViewModel
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailViewModel
import net.metalbrain.paysmart.core.features.theme.viewmodel.AppThemeViewModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel
import net.metalbrain.paysmart.core.session.SessionViewModel
import net.metalbrain.paysmart.core.features.account.recovery.viewmodel.ChangePhoneRecoveryViewModel
import net.metalbrain.paysmart.core.features.identity.screen.IdentityUploadScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityVerifyScreen
import net.metalbrain.paysmart.core.features.identity.screen.IdentityThirdPartyProviderScreen
import net.metalbrain.paysmart.core.features.invoicing.utils.InvoiceWeeklyEntryRoute
import net.metalbrain.paysmart.utils.formatPhoneNumberForDisplay

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
    object LoginMfaChallenge : Screen("login/mfa_challenge")

    object Reauthenticate: Screen("reauthenticate?target={target}") {
        const val BASEROUTE = "reauthenticate"
        const val baseRoute = BASEROUTE
        fun routeWithTarget(target: String): String =
            "reauthenticate?target=${Uri.encode(target)}"
    }

    object EnterPassword: Screen("enter_password")


    object ProfileScreen : Screen("profile")
    object ProfilePhotoPicker : Screen("profile/photo")

    object ProfileAccountInformation : Screen("profile/account_information")

    object ProfileSecurityPrivacy : Screen("profile/security_privacy")
    object ProfileChangePasscodeGate : Screen("profile/security_privacy/passcode_gate")
    object ProfileChangePasscode : Screen("profile/security_privacy/passcode_change")
    object ProfilePasskeySettings : Screen("profile/security_privacy/passkey")

    object ProfileConnectedAccounts : Screen("profile/connected_accounts")

    object ProfileAbout : Screen("profile/about")
    object ProfileAboutSocials : Screen("profile/about/socials")

    object ProfileIdentity : Screen("profile/account_information/identity")
    object OnboardingProfile : Screen("onboarding/profile_identity")

    object ProfileAccountLimits : Screen("profile/account_information/account_limits")

    object ProfileAccountLimitsDetails :
        Screen("profile/account_information/account_limits/account_limit_details?currencyCode={currencyCode}") {
        const val BASEROUTE = "profile/account_information/account_limits/account_limit_details"
        const val CURRENCYARG = "currencyCode"

        fun routeWithCurrency(currencyCode: String): String {
            return "$BASEROUTE?$CURRENCYARG=${Uri.encode(currencyCode.trim())}"
        }
    }


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
    object FundingAccount : Screen("wallet/funding_account")
    object SendMoney : Screen("wallet/send_money")
    object InvoiceFlow : Screen("invoice_flow")
    object InvoiceWorkerProfile : Screen("invoice/profile")
    object InvoiceVenueSetup : Screen("invoice/venue")
    object InvoiceWeeklyEntry : Screen("invoice/weekly")
    object InvoiceDetail : Screen("invoice/detail/{invoiceId}") {
        fun routeWithInvoiceId(invoiceId: String): String {
            return "invoice/detail/${Uri.encode(invoiceId)}"
        }
    }

    object FeatureGate : Screen("feature_gate?feature={feature}&resumeRoute={resumeRoute}") {
        const val BASEROUTE = "feature_gate"
        const val FEATUREARG = "feature"
        const val RESUMEROUTEARG = "resumeRoute"

        fun routeWithArgs(feature: String, resumeRoute: String): String {
            return "$BASEROUTE?$FEATUREARG=${Uri.encode(feature)}&$RESUMEROUTEARG=${Uri.encode(resumeRoute)}"
        }
    }

    object BalanceDetails : Screen("wallet_balance?currencyCode={currencyCode}&tab={tab}") {
        const val BASEROUTE = "wallet_balance"
        const val CURRENCYARG = "currencyCode"
        const val TAB_ARG = "tab"

        enum class Tab(val routeValue: String) {
            TRANSACTIONS("transactions"),
            ACCOUNT_DETAILS("account_details");

            companion object {
                fun fromRouteValue(raw: String?): Tab {
                    return entries.firstOrNull { it.routeValue == raw } ?: TRANSACTIONS
                }
            }
        }

        fun routeWithCurrency(
            currencyCode: String,
            tab: Tab = Tab.TRANSACTIONS
        ): String {
            return "$BASEROUTE?$CURRENCYARG=${Uri.encode(currencyCode.trim())}&$TAB_ARG=${Uri.encode(tab.routeValue)}"
        }
    }

    object ExchangeRates : Screen("fx/exchange_rates?countryIso2={countryIso2}") {
        const val BASEROUTE = "fx/exchange_rates"
        const val COUNTRY_ISO2_ARG = "countryIso2"

        fun routeWithCountry(countryIso2: String): String {
            return "$BASEROUTE?$COUNTRY_ISO2_ARG=${Uri.encode(countryIso2.trim())}"
        }
    }

    object RewardDetails : Screen("reward_earned")

    object Transactions: Screen("transactions")

    object TransactionDetail : Screen("transactions/detail/{transactionId}") {
        fun routeWithTransactionId(transactionId: String): String {
            return "transactions/detail/${Uri.encode(transactionId)}"
        }
    }

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

private fun openExternalUri(context: Context, uri: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun openSupportEmailComposer(context: Context, emailAddress: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:${Uri.encode(emailAddress)}".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun openAppRating(context: Context): Boolean {
    val packageName = context.packageName
    val marketIntent = Intent(
        Intent.ACTION_VIEW,
        "market://details?id=$packageName".toUri()
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    return try {
        context.startActivity(marketIntent)
        true
    } catch (_: ActivityNotFoundException) {
        openExternalUri(
            context = context,
            uri = "https://play.google.com/store/apps/details?id=$packageName"
        )
    }
}

private fun NavHostController.navigateInGraph(
    route: String,
    source: String = "nav_graph",
    suppressSameRoute: Boolean = true,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigateSafely(
        route = route,
        currentRoute = currentDestination?.route,
        source = source,
        suppressSameRoute = suppressSameRoute,
        builder = builder,
    )
}

private fun NavHostController.navigateClearingBackStackInGraph(
    route: String,
    source: String = "nav_graph",
    inclusive: Boolean = true,
    suppressSameRoute: Boolean = false,
) {
    navigateClearingBackStackSafely(
        route = route,
        currentRoute = currentDestination?.route,
        source = source,
        inclusive = inclusive,
        suppressSameRoute = suppressSameRoute,
    )
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    LaunchedEffect(activity?.intent?.dataString) {
        val deepLink = activity?.intent?.data
        val callbackPath = deepLink?.path.orEmpty()
        if (callbackPath.startsWith("/verify/identity/provider")) {
            val event = deepLink?.getQueryParameter("event").orEmpty().ifBlank { "sdk_callback" }
            val sessionId = deepLink?.getQueryParameter("sessionId")
            val providerRef = deepLink?.getQueryParameter("providerRef")
            navController.navigateInGraph(
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
            navController.navigateInGraph(Screen.Login.route) {
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
            val loginViewModel: LoginViewModel = hiltViewModel()
            val hostActivity = LocalActivity.current as? FragmentActivity

            LaunchedEffect(hostActivity) {
                val activity = hostActivity ?: return@LaunchedEffect
                loginViewModel.signInWithPasskey(
                    activity = activity,
                    autoAttempt = true,
                    onSuccess = {
                        navController.navigateInGraph(Screen.Home.route) {
                            popUpTo(Screen.Startup.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onError = {
                        Log.d("NavGraph", "Startup auto passkey attempt skipped: ${it.localizedMessage}")
                    }
                )
            }

            StartupScreen(
                navController = navController,
                onLoginClick = {
                    navController.navigateInGraph(Screen.Login.route)
                },
                onCreateAccountClick = {
                    navController.navigateInGraph(Screen.CreateAccount.route)
                },
                viewModel = viewModel,
            )
        }

        composable(Screen.ProtectAccount.route) {
            AccountProtectionContent(
                onSetPasscodeClick = {
                    navController.navigateInGraph(Screen.SetUpPassCode.route)
                },
                onSetBiometricClick = {
                    navController.navigateInGraph(Screen.BiometricOptIn.route)
                },
                onSetPasskeyClick = {
                    navController.navigateInGraph(Screen.PasskeySetup.route)
                }
            )
        }

        composable(Screen.PasskeySetup.route) {
            val viewModel: PasskeySetupViewModel = hiltViewModel()
            val activity = LocalActivity.current as FragmentActivity
            PasskeySetupScreen(
                activity = activity,
                viewModel = viewModel,
                onRegistered = {
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.PasskeySetup.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
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
                    navController.navigateInGraph(targetRoute) {
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
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.RequireSessionUnlock.route) { inclusive = true }
                    }
                }
            }
            val onLogout = {
                userViewModel.signOut()
                navController.navigateClearingBackStackInGraph(
                    route = Screen.Startup.route,
                    source = "nav_graph_logout",
                )
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
                            navController.navigateInGraph(Screen.SetUpPassCode.route) {
                                launchSingleTop = true
                            }
                        },
                        onSetBiometricClick = {
                            navController.navigateInGraph(Screen.BiometricOptIn.route) {
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
                    navController.navigateInGraph(targetRoute) {
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
                    navController.navigateInGraph(Screen.PostOtpCapabilities.routeWithCountry(countryIso2)) {
                        popUpTo(Screen.OtpVerification.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                viewModel = otpViewModel,
            )
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
                    navController.navigateInGraph(Screen.PostOtpSecuritySteps.routeWithCountry(countryIso2)) {
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
                    navController.navigateInGraph(Screen.PostOtpClientInformation.routeWithCountry(countryIso2)) {
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
                    navController.navigateInGraph(
                        Screen.LinkFederatedAccount.routeWithReturn(Screen.ProtectAccount.route)
                    ) {
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
            val userViewModel: UserViewModel = hiltViewModel()
            val currentDestinationId = backStackEntry.destination.id

            MfaNudgeScreen(
                viewModel = mfaViewModel,
                onBack = { navController.popBackStack() },
                onPrimaryAction = { hasVerifiedEmail ->
                    if (hasVerifiedEmail) {
                        navController.navigateInGraph(Screen.ProtectAccount.route) {
                            popUpTo(currentDestinationId) { inclusive = true }
                        }
                    } else {
                        navController.navigateInGraph(
                            Screen.LinkFederatedAccount.routeWithReturn(Screen.ProtectAccount.route)
                        )
                    }
                },
                onBlockedAction = {
                    userViewModel.signOut()
                    navController.navigateClearingBackStackInGraph(
                        route = Screen.Login.route,
                        source = "mfa_unsupported_first_factor",
                    )
                },
                onSkip = {
                    navController.navigateInGraph(Screen.ProtectAccount.route) {
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
                    navController.navigateInGraph(
                        Screen.OtpVerification.routeWithArgs(
                            dialCode = dialCode,
                            phoneNumber = rawPhone,
                            countryIso2 = countryIso2
                        )
                    )
                },
                onGetHelpClicked = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                },
                onSignInClicked = {
                    navController.navigateInGraph(Screen.Login.route) {
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
                    navController.navigateInGraph(destination) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGoogleLinkSuccess = { navController.navigateInGraph(destination) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onFacebookLinkSuccess = {
                    navController.navigateInGraph(destination) {
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
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onMfaRequired = {
                    navController.navigateInGraph(Screen.LoginMfaChallenge.route) {
                        launchSingleTop = true
                    }
                },
                onForgotPassword = {
                    navController.navigateInGraph(Screen.RecoverAccount.routeWithOrigin("login"))
                },
                onSignUp = {
                    navController.navigateInGraph(Screen.CreateAccount.route)
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LoginMfaChallenge.route) {
            val viewModel: MfaSignInViewModel = hiltViewModel()
            MfaSignInChallengeScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
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
                        navController.navigateInGraph(recoveryTarget) {
                            popUpTo(Screen.Reauthenticate.route) { inclusive = true }
                        }
                    } else {
                        navController.navigateInGraph(Screen.EnterPassword.route) {
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
                    navController.navigateInGraph(Screen.Home.route) {
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
                        navController.navigateInGraph(
                            Screen.Reauthenticate.routeWithTarget(
                                Screen.ChangePasswordRecovery.route
                            )
                        )
                    } else {
                        navController.navigateInGraph(Screen.ChangePasswordRecovery.route)
                    }
                },
                onChangePhoneClick = {
                    if (origin == "login") {
                        navController.navigateInGraph(
                            Screen.Reauthenticate.routeWithTarget(
                                Screen.ChangePhoneRecovery.route
                            )
                        )
                    } else {
                        navController.navigateInGraph(Screen.ChangePhoneRecovery.route)
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
                    navController.navigateClearingBackStackInGraph(
                        route = Screen.Home.route,
                        source = "create_password_complete",
                    )
                }
            )
        }

        composable(Screen.SetUpPassCode.route) {
            val passCodeviewModel: PasscodeViewModel = hiltViewModel()
            SetPasscodeScreen(
                viewModel = passCodeviewModel,
                onPasscodeSet = {
                    Log.d("SetPasscodeScreen", "onPasscodeSet invoked")
                    navController.navigateInGraph(Screen.Home.route) {
                        popUpTo(Screen.SetUpPassCode.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileChangePasscodeGate.route) {
            ChangePasscodeBiometricGateScreen(
                onBack = { navController.popBackStack() },
                onVerified = {
                    navController.navigateInGraph(Screen.ProfileChangePasscode.route) {
                        popUpTo(Screen.ProfileChangePasscodeGate.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileChangePasscode.route) {
            ChangePasscodeScreen(
                onBack = { navController.popBackStack() },
                onPasscodeChanged = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.VerifyPasscode.route) {
            VerifyPasscodeScreen(
                onVerified = {
                    navController.navigateInGraph(Screen.Home.route) {
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
                    navController.navigateClearingBackStackInGraph(
                        route = returnRoute,
                        source = "email_verification_success",
                    )
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
            val decision = settings?.let {
                FeatureAccessPolicy.evaluate(feature, it)
            }

            LaunchedEffect(localSecurityState, decision?.isAllowed, feature, resumeRoute) {
                Log.d(
                    "FeatureGateDiag",
                    "feature=${feature.id} " +
                        "state=${localSecurityState::class.simpleName} " +
                        "resumeRoute=$resumeRoute " +
                        "settingsReady=${settings != null} " +
                        "allowed=${decision?.isAllowed} " +
                        "missing=${decision?.missingRequirements.orEmpty()} " +
                        "strength=${decision?.currentSecurityStrength} " +
                        "required=${decision?.requiredSecurityStrength}"
                )
            }

            LaunchedEffect(decision?.isAllowed, resumeRoute) {
                if (decision?.isAllowed == true) {
                    navController.navigateInGraph(resumeRoute) {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            if (localSecurityState is LocalSecurityState.Loading || decision == null) {
                SplashScreen()
                return@composable
            }

            FeatureGateScreen(
                feature = feature,
                decision = decision,
                onContinue = {
                    when (decision.nextRequirement) {
                        FeatureRequirement.VERIFIED_EMAIL -> {
                            navController.navigateInGraph(
                                "${Screen.AddEmail.route}?returnRoute=${Uri.encode(gateRoute)}"
                            )
                        }

                        FeatureRequirement.HOME_ADDRESS_VERIFIED -> {
                            navController.navigateInGraph(Screen.ProfileAddressResolver.route)
                        }

                        FeatureRequirement.IDENTITY_VERIFIED -> {
                            navController.navigateInGraph(Screen.ProfileIdentityResolver.route)
                        }

                        FeatureRequirement.SECURITY_STRENGTH_TWO -> {
                            navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route) {
                                launchSingleTop = true
                            }
                        }

                        null -> {
                            navController.navigateInGraph(resumeRoute) {
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
                onBack = { navController.popBackStack() },
                onOpenAccountDetails = { currencyCode ->
                    navController.navigateInGraph(
                        Screen.BalanceDetails.routeWithCurrency(
                            currencyCode = currencyCode,
                            tab = Screen.BalanceDetails.Tab.ACCOUNT_DETAILS
                        )
                    )
                }
            )
        }

        composable(Screen.FundingAccount.route) {
            val viewModel: FundingAccountViewModel = hiltViewModel()
            FundingAccountRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SendMoney.route) {
            SendMoneyRecipientScreen(
                onBack = { navController.popBackStack() }
            )
        }

        navigation(
            route = Screen.InvoiceFlow.route,
            startDestination = Screen.InvoiceWorkerProfile.route
        ) {
            composable(Screen.InvoiceWorkerProfile.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InvoiceFlow.route)
                }
                val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
                InvoiceWorkerProfileRoute(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onContinue = {
                        navController.navigateInGraph(Screen.InvoiceVenueSetup.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.InvoiceVenueSetup.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InvoiceFlow.route)
                }
                val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
                InvoiceVenueSetupRoute(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRequireProfile = {
                        navController.navigateInGraph(Screen.InvoiceWorkerProfile.route) {
                            launchSingleTop = true
                        }
                    },
                    onContinue = {
                        navController.navigateInGraph(Screen.InvoiceWeeklyEntry.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.InvoiceWeeklyEntry.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.InvoiceFlow.route)
                }
                val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
                InvoiceWeeklyEntryRoute(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRequireProfileSetup = {
                        navController.navigateInGraph(Screen.InvoiceWorkerProfile.route) {
                            launchSingleTop = true
                        }
                    },
                    onRequireVenueSetup = {
                        navController.navigateInGraph(Screen.InvoiceVenueSetup.route) {
                            launchSingleTop = true
                        }
                    },
                    onOpenInvoice = { invoiceId ->
                        navController.navigateInGraph(Screen.InvoiceDetail.routeWithInvoiceId(invoiceId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val viewModel: InvoiceDetailViewModel = hiltViewModel()
            val invoiceId = Uri.decode(backStackEntry.arguments?.getString("invoiceId").orEmpty())
            InvoiceDetailRoute(
                invoiceId = invoiceId,
                viewModel = viewModel,
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
                navArgument(Screen.BalanceDetails.CURRENCYARG) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(Screen.BalanceDetails.TAB_ARG) {
                    type = NavType.StringType
                    defaultValue = Screen.BalanceDetails.Tab.TRANSACTIONS.routeValue
                }
            )
        ) { backStackEntry ->
            val viewModel: BalanceDetailsViewModel = hiltViewModel()
            val initialTab = Screen.BalanceDetails.Tab.fromRouteValue(
                backStackEntry.arguments?.getString(Screen.BalanceDetails.TAB_ARG)
            )
            BalanceDetailsRoute(
                viewModel = viewModel,
                initialTab = initialTab,
                onBack = { navController.popBackStack() },
                onViewAccountLimitsClick = { currencyCode ->
                    navController.navigateInGraph(
                        Screen.ProfileAccountLimitsDetails.routeWithCurrency(currencyCode)
                    ) {
                        launchSingleTop = true
                    }
                },
                onSendClick = {
                    navController.navigateInGraph(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                },
                onAddClick = {
                    navController.navigateInGraph(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.ADD_MONEY.id,
                            resumeRoute = Screen.AddMoney.route
                        )
                    )
                },
                onWithdrawClick = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                },
                onConvertClick = {
                    navController.navigateInGraph(Screen.SendMoney.route) {
                        launchSingleTop = true
                    }
                },
                onTransactionClick = { transaction ->
                    navController.navigateInGraph(Screen.TransactionDetail.routeWithTransactionId(transaction.id))
                }
            )
        }

        composable(
            route = Screen.ExchangeRates.route,
            arguments = listOf(
                navArgument(Screen.ExchangeRates.COUNTRY_ISO2_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            val viewModel: ExchangeRatesViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsState()
            ExchangeRatesScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onRefresh = viewModel::refresh,
                onSendClick = {
                    navController.navigateInGraph(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                }
            )
        }

        composable(Screen.RewardDetails.route) {
            val viewModel: RewardDetailsViewModel = hiltViewModel()
            RewardDetailsRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onHelpClick = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                },
                onTransactionClick = { transaction ->
                    navController.navigateInGraph(Screen.TransactionDetail.routeWithTransactionId(transaction.id))
                }
            )
        }

        composable(Screen.Transactions.route){
            TransactionsScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.TransactionDetail.route,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = Uri.decode(backStackEntry.arguments?.getString("transactionId") ?: "")
            val transactionDetailViewModel: TransactionDetailViewModel = hiltViewModel()
            TransactionDetailRoute(
                transactionId = transactionId,
                viewModel = transactionDetailViewModel,
                onBack = { navController.popBackStack() }
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
                    onChangePhotoClick = {
                        navController.navigateInGraph(Screen.ProfilePhotoPicker.route)
                    },
                    onAccountInformationClick = {
                        navController.navigateInGraph(Screen.ProfileAccountInformation.route)
                    },
                    onSecurityPrivacyClick = {
                        navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route)
                    },
                    onConnectedAccountsClick = {
                        navController.navigateInGraph(Screen.ProfileConnectedAccounts.route)
                    },
                    onHelpAndSupportClick = {
                        navController.navigateInGraph(Screen.Help.route)
                    },
                    onAboutClick = {
                        navController.navigateInGraph(Screen.ProfileAbout.route)
                    },
                    onLogout = {
                        navController.navigateClearingBackStackInGraph(
                            route = Screen.Startup.route,
                            source = "profile_logout",
                        )
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.ProfilePhotoPicker.route) {
            val userViewModel: UserViewModel = hiltViewModel()
            val photoViewModel: ProfilePhotoViewModel = hiltViewModel()
            val userState by userViewModel.uiState.collectAsState()
            val photoState by photoViewModel.uiState.collectAsState()

            if (userState is UserUiState.ProfileLoaded) {
                ProfilePhotoPickerRoute(
                    user = (userState as UserUiState.ProfileLoaded).user,
                    uiState = photoState,
                    viewModel = photoViewModel,
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
                    navController.navigateInGraph(Screen.ProfileIdentity.route)
                },
                onAccountLimitsClick = {
                    navController.navigateInGraph(Screen.ProfileAccountLimits.route)
                },
                onAccountStatementClick = {
                    navController.navigateInGraph(Screen.ProfileAccountStatement.route)
                },
                onLanguageClick = {
                    navController.navigateInGraph(
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
            val context = LocalContext.current
            val settings = (localSecurityState as? LocalSecurityState.Ready)?.settings

            ProfileSecurityPrivacyScreen(
                settings = settings,
                hideBalanceEnabled = hideBalanceEnabled,
                onBack = { navController.popBackStack() },
                onResetPassword = {
                    navController.navigateInGraph(Screen.ChangePasswordRecovery.route) {
                        launchSingleTop = true
                    }
                },
                onTransactionPin = {
                    val isPasscodeReady =
                        settings?.passcodeEnabled == true && settings.localPassCodeSetAt != null
                    val shouldUseBiometricGate =
                        settings?.biometricsEnabled == true &&
                            BiometricHelper.isBiometricAvailable(context)
                    val targetRoute = when {
                        !isPasscodeReady -> Screen.SetUpPassCode.route
                        shouldUseBiometricGate -> Screen.ProfileChangePasscodeGate.route
                        else -> Screen.ProfileChangePasscode.route
                    }
                    navController.navigateInGraph(targetRoute) {
                        launchSingleTop = true
                    }
                },
                onMfaSettings = {
                    navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                        launchSingleTop = true
                    }
                },
                onPasskeySettings = {
                    navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
                        launchSingleTop = true
                    }
                },
                onBiometricToggle = { enabled ->
                    if (enabled) {
                        navController.navigateInGraph(Screen.BiometricOptIn.route) {
                            launchSingleTop = true
                        }
                    } else {
                        securityViewModel.clearBiometricOptIn()
                    }
                },
                onViewPrivacySettings = {
                    navController.navigateInGraph(Screen.ProfileAbout.route) {
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
            val userViewModel: UserViewModel = hiltViewModel()
            val currentDestinationId = backStackEntry.destination.id

            MfaNudgeScreen(
                viewModel = mfaViewModel,
                onBack = { navController.popBackStack() },
                onPrimaryAction = { hasVerifiedEmail ->
                    if (hasVerifiedEmail) {
                        navController.navigateInGraph(Screen.ProfileSecurityPrivacy.route) {
                            popUpTo(currentDestinationId) { inclusive = true }
                        }
                    } else {
                        navController.navigateInGraph(
                            "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileMfaNudge.route)}"
                        )
                    }
                },
                onBlockedAction = {
                    userViewModel.signOut()
                    navController.navigateClearingBackStackInGraph(
                        route = Screen.Login.route,
                        source = "mfa_unsupported_first_factor",
                    )
                },
                onSkip = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileConnectedAccounts.route) {
            val viewModel: FundingAccountViewModel = hiltViewModel()
            val managedCardsViewModel: ManagedCardsViewModel = hiltViewModel()
            ProfileConnectedAccountsRoute(
                viewModel = viewModel,
                managedCardsViewModel = managedCardsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ProfileAbout.route) {
            val actionOpenFailedMessage =
                stringResource(R.string.profile_about_open_action_failed)
            val legalUrl = stringResource(R.string.profile_about_legal_url)
            val blogUrl = stringResource(R.string.profile_about_blog_url)
            val contactUrl = stringResource(R.string.profile_about_contact_url)
            val contactEmail = stringResource(R.string.profile_about_contact_email)

            fun openUriOrToast(uri: String) {
                if (!openExternalUri(context, uri)) {
                    Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                }
            }

            ProfileAboutScreen(
                onBack = { navController.popBackStack() },
                onLegalClick = { openUriOrToast(legalUrl) },
                onSocialMediaClick = {
                    navController.navigateInGraph(Screen.ProfileAboutSocials.route) {
                        launchSingleTop = true
                    }
                },
                onBlogClick = { openUriOrToast(blogUrl) },
                onAppRatingClick = {
                    if (!openAppRating(context)) {
                        Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                    }
                },
                onContactUsClick = {
                    val openedEmail = openSupportEmailComposer(context, contactEmail)
                    if (!openedEmail && !openExternalUri(context, contactUrl)) {
                        Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable(Screen.ProfileAboutSocials.route) {
            val actionOpenFailedMessage =
                stringResource(R.string.profile_about_open_action_failed)
            val xUrl = stringResource(R.string.profile_about_social_x_url)
            val instagramUrl = stringResource(R.string.profile_about_social_instagram_url)
            val linkedInUrl = stringResource(R.string.profile_about_social_linkedin_url)
            val facebookUrl = stringResource(R.string.profile_about_social_facebook_url)

            fun openUriOrToast(uri: String) {
                if (!openExternalUri(context, uri)) {
                    Toast.makeText(context, actionOpenFailedMessage, Toast.LENGTH_SHORT).show()
                }
            }

            ProfileAboutSocialsScreen(
                onBack = { navController.popBackStack() },
                onXClick = { openUriOrToast(xUrl) },
                onInstagramClick = { openUriOrToast(instagramUrl) },
                onLinkedInClick = { openUriOrToast(linkedInUrl) },
                onFacebookClick = { openUriOrToast(facebookUrl) }
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
                                navController.navigateInGraph(
                                    "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.ProfileIdentity.route)}"
                                )
                            }

                            ProfileNextStep.COMPLETE_ADDRESS,
                            ProfileNextStep.VERIFY_IDENTITY -> {
                                navController.navigateInGraph(
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
                        navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                            launchSingleTop = true
                        }
                    },
                    showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                    onOpenPasskeyNudge = {
                        navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
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
                                navController.navigateInGraph(
                                    Screen.LinkFederatedAccount.routeWithReturn(Screen.OnboardingProfile.route)
                                )
                            }

                            ProfileNextStep.COMPLETE_ADDRESS,
                            ProfileNextStep.VERIFY_IDENTITY -> {
                                navController.navigateInGraph(
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
                        navController.navigateInGraph(Screen.ProtectAccount.route) {
                            popUpTo(Screen.OnboardingProfile.route) { inclusive = true }
                        }
                    },
                    showMfaNudgeCta = profileState.security?.hasSkippedMfaEnrollmentPrompt == true,
                    onOpenMfaNudge = {
                        navController.navigateInGraph(Screen.ProfileMfaNudge.route) {
                            launchSingleTop = true
                        }
                    },
                    showPasskeyNudgeCta = profileState.security?.hasSkippedPasskeyEnrollmentPrompt == true,
                    onOpenPasskeyNudge = {
                        navController.navigateInGraph(Screen.ProfilePasskeySettings.route) {
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
                        navController.navigateInGraph(Screen.ProfileIdentityResolverVerify.route) {
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
                        navController.navigateInGraph(Screen.ProfileIdentityResolverUpload.route) {
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

            AccountLimitsRoute(
                settings = settings,
                onBack = { navController.popBackStack() },
                onAccountClick = { currencyCode ->
                    navController.navigateInGraph(
                        Screen.ProfileAccountLimitsDetails.routeWithCurrency(currencyCode)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ProfileAccountLimitsDetails.route,
            arguments = listOf(
                navArgument(Screen.ProfileAccountLimitsDetails.CURRENCYARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            AccountLimitDetailsRoute(
                onBack = { navController.popBackStack() },
                onHelp = {
                    navController.navigateInGraph(Screen.Help.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.ProfileAccountStatement.route) {
            val accountStatementViewModel: AccountStatementViewModel = hiltViewModel()

            AccountStatementRoute(
                viewModel = accountStatementViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
