package net.metalbrain.paysmart.navigator

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.firebase.auth.FirebaseAuth
import net.metalbrain.paysmart.domain.model.DEFAULT_COUNTRY_ISO2
import net.metalbrain.paysmart.domain.model.LaunchInterest
import net.metalbrain.paysmart.domain.state.UserUiState

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
    startDestination: String = Screen.Home.route,
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
        startDestination = startDestination
    ) {

        authNavGraph(navController)
        walletNavGraph(navController)
        invoiceNavGraph(navController)

        homeNavGraph(navController)
        profileNavGraph(navController)
    }
}
