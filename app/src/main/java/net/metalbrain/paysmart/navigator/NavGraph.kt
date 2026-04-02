package net.metalbrain.paysmart.navigator


import android.net.Uri
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.compose.LocalActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import com.google.firebase.auth.FirebaseAuth
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.model.normalizeCountryIso2
import net.metalbrain.paysmart.domain.state.UserUiState

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

    object CreatePassword : Screen("create_password?returnRoute={returnRoute}") {
        const val BASEROUTE = "create_password"
        const val RETURN_ROUTE_ARG = "returnRoute"

        fun routeWithReturn(returnRoute: String): String {
            return "$BASEROUTE?$RETURN_ROUTE_ARG=${Uri.encode(returnRoute)}"
        }
    }

    object SetUpPassCode : Screen("set_up_passcode")

    object VerifyPasscode : Screen("verify_passcode")

    object AddEmail : Screen("add_email")

    object EmailSent : Screen("email_sent/{email}") {
        fun routeWithEmail(email: String) = "email_sent/${Uri.encode(email)}"
        fun routeWithArgs(email: String, returnRoute: String): String {
            return "${routeWithEmail(email)}?returnRoute=${Uri.encode(returnRoute)}"
        }
    }

    object EmailVerified : Screen("email_verified") {
        const val RETURN_ROUTE_ARG = "returnRoute"
        fun routeWithReturn(returnRoute: String): String {
            return "${route}?$RETURN_ROUTE_ARG=${Uri.encode(returnRoute)}"
        }
    }

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
    object ProfilePrivacySettings : Screen("profile/privacy_settings")
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
    object ProfileIdentityResolverIntro : Screen("profile/setup/identity_resolver/local/info")
    object ProfileIdentityResolverVerify : Screen("profile/setup/identity_resolver/local/verify")
    object ProfileIdentityResolverUpload : Screen("profile/setup/identity_resolver/local/upload")
    object ProfileIdentityResolverPending : Screen("profile/setup/identity_resolver/local/pending")
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
    object NotificationCenter : Screen("notifications")
    object AddMoney : Screen("wallet/add_money")
    object FundingAccount : Screen("wallet/funding_account")
    object UkAccount : Screen("wallet/uk_account?currencyCode={currencyCode}") {
        const val BASEROUTE = "wallet/uk_account"
        const val CURRENCYARG = BalanceDetails.CURRENCYARG

        fun routeWithCurrency(currencyCode: String = "GBP"): String {
            return "$BASEROUTE?$CURRENCYARG=${Uri.encode(currencyCode.trim())}"
        }
    }
    object SendMoney : Screen("wallet/send_money") {
        const val RECIPIENT_KEY_ARG = "recipientKey"

        fun routeWithRecipientKey(recipientKey: String): String {
            return "${route}?$RECIPIENT_KEY_ARG=${Uri.encode(recipientKey.trim())}"
        }
    }
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

internal fun resolveLanguageContinueRoute(origin: Screen.Origin): String {
    return when (origin) {
        Screen.Origin.LOGIN -> Screen.Login.route
        Screen.Origin.CREATE_ACCOUNT -> Screen.CreateAccount.route
        Screen.Origin.PROFILE_ACCOUNT_INFORMATION -> Screen.ProfileAccountInformation.route
        Screen.Origin.STARTUP -> Screen.Startup.route
    }
}

internal fun openEmailApp(context: Context): Boolean {
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

internal fun openExternalUri(context: Context, uri: String): Boolean {
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

fun openSupportEmailComposer(context: Context, emailAddress: String): Boolean {
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

fun openAppRating(context: Context): Boolean {
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

fun NavHostController.navigateInGraph(
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

fun NavHostController.navigateClearingBackStackInGraph(
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

internal fun resolveLaunchInterest(userState: UserUiState, countryIso2: String? = DEFAULT_COUNTRY_ISO2): LaunchInterest {
    return (userState as? UserUiState.ProfileLoaded)?.user?.launchInterest
        ?: LaunchInterest.defaultForCountry(countryIso2)
}

internal fun createPasswordRouteForLaunchInterest(
    launchInterest: LaunchInterest,
    returnRoute: String = Screen.InvoiceFlow.route
): String {
    return if (launchInterest == LaunchInterest.INVOICE) {
        Screen.CreatePassword.routeWithReturn(returnRoute)
    } else {
        Screen.CreatePassword.BASEROUTE
    }
}

internal fun homeDestinationForLaunchInterest(
    launchInterest: LaunchInterest
): String {
    return if (launchInterest == LaunchInterest.INVOICE) {
        Screen.InvoiceFlow.route
    } else {
        Screen.Home.route
    }
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

        if (
            callbackPath.startsWith("/verify") &&
            deepLink?.getQueryParameter("mode").orEmpty() == "verifyEmail"
        ) {
            val returnRoute = Uri.decode(
                deepLink?.getQueryParameter(Screen.EmailVerified.RETURN_ROUTE_ARG)
                    ?: Screen.Home.route
            )
            navController.navigateInGraph(
                Screen.EmailVerified.routeWithReturn(returnRoute)
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

        authNavGraph(navController)
        walletNavGraph(navController)
        invoiceNavGraph(navController)

        homeNavGraph(navController)
        profileNavGraph(navController)
    }
}
